<?php
/**
 * This script downloads all announcements in Norsk Lovtidend, released by Ministry of Justice, published by Lovdata.
 *
 * Pages within the current year are cached for 4 days. All pages other pages are cached for 365 days. There can be changes in old
 * announcements.
 *
 * @author Hallvard Nygård, @hallny / Norske-postlister.no.
 */

set_error_handler(function ($errno, $errstr, $errfile, $errline, array $errcontext) {
    throw new ErrorException($errstr, 0, $errno, $errfile, $errline);
});

require __DIR__ . '/vendor/autoload.php';
use Symfony\Component\DomCrawler\Crawler;

$cacheTimeSecondsThisYear_paging = 60 * 60 * 24 * 4;
$cacheTimeSecondsThisYear = 60 * 60 * 24 * 20;
$cacheTimeSecondsPrevYears = 60 * 60 * 24 * 365;
$cache_location = __DIR__ . '/cache';
$lovdataNo = 'https://lovdata.no';
$baseUrl = $lovdataNo . '/register/lovtidend';
$updateDate = date('d.m.Y H:i:s');

$lawsNotPresent = array();

$obj = new stdClass();
$obj->lastUpdated = $updateDate;
$obj->sourceInfo = "Datasett hentet fra https://hnygard.github.io/norsk-lovtidend/, laget av @hallny. Kilde: $baseUrl";
$obj->itemCountLaws = 0;
$obj->itemCountNationalRegulations = 0;
$obj->itemCountLocalRegulations = 0;
$obj->announcementsPerYear = array();

for($year = date('Y'); $year >= 2001; $year--) {
    if (isset($argv[1]) && $year != $argv[1]) {
        continue;
    }

    $objYear = new stdClass();
    $obj->announcementsPerYear[] = $objYear;
    $objYear->pageCount = 1;
    $objYear->itemCount = 0;
    $objYear->itemsLaw = array();
    $objYear->itemsLocalRegulation = array();
    $objYear->itemsNationalRegulation = array();

	$cacheTimeSeconds = date('Y') == $year ? $cacheTimeSecondsThisYear : $cacheTimeSecondsPrevYears;
	$cacheTimeSeconds_paging = date('Y') == $year ? $cacheTimeSecondsThisYear_paging : $cacheTimeSecondsPrevYears;
	mkdirIfNotExists($cache_location . '/' . $year . '/paged-list');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTI-lov');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTI-forskrift');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTII-forskrift');
    mkdirIfNotExists(__DIR__  . '/norway-law-java/src/main/resources/laws/' . $year . '/');
	$offset = 0;
	$maxOffset = 10;
	while ($offset <= $maxOffset) {
		$mainPage = getUrlCachedUsingCurl(
            $cacheTimeSeconds_paging,
			$cache_location . '/' . $year . '/paged-list/offset-' . $offset . '.html',
			$baseUrl . '?year=' . $year . '&offset=' . $offset
		);
		$items = readItems($mainPage);
		logInfo($year . ' - Offset ' . $offset . ' of ' .  $items['lastItem'] . ' - ' . $items['pages']);
		$offset = $offset + 20;
        $objYear->pageCount++;
		if ($maxOffset == 10) {
			$maxOffset = $items['lastItem'];
		}

		if (count($items['links']) != ($items['itemN'] - $items['item1'] + 1)) {
			var_dump($items['links']);
			throw new Exception('Count(items) != paging counter' . chr(10)
				. count($items['links']) .' != ' . ($items['itemN'] - $items['item1'] + 1)
			);
		}

		// For each announcement => download
		foreach ($items['links'] as $link) {
			if (str_starts_with($link['href'], '/dokument/LTI/forskrift/')) {
				// Example: /dokument/LTI/forskrift/2020-02-04-107
				$cacheFolder = 'LTI-forskrift';
				$cacheHtmlName = str_replace('/dokument/LTI/forskrift/', '', $link['href']);
			}
			elseif (str_starts_with($link['href'], '/dokument/LTII/forskrift/')) {
				// Example: /dokument/LTII/forskrift/2020-01-30-108
				$cacheFolder = 'LTII-forskrift';
				$cacheHtmlName = str_replace('/dokument/LTII/forskrift/', '', $link['href']);
			}
			elseif (str_starts_with($link['href'], '/dokument/LTI/lov/')) {
				// Example: /dokument/LTI/lov/2020-01-10-1
				$cacheFolder = 'LTI-lov';
				$cacheHtmlName = str_replace('/dokument/LTI/lov/', '', $link['href']);
			}
			else {
				var_dump($link);
				throw new Exception('Unknown link.');
			}

			if (isset($argv[2]) && !str_contains($link['href'], $argv[2])) {
			    continue;
            }

            $objAnn = new stdClass();
            $objAnn->url = $lovdataNo . $link['href'];

			$announcementCacheFile = $cache_location . '/' . $year . '/' . $cacheFolder . '/' . $cacheHtmlName . '.html';
			$announcement = getUrlCachedUsingCurl(
				$cacheTimeSeconds,
				$announcementCacheFile,
				$lovdataNo . $link['href']
			);
            $objYear->itemCount++;

            $announcement = str_replace(
                '&#x1F517;</i><span class="share-paragraf-title">Del paragraf</span>',
                '</i><span class="share-paragraf-title"></span>',
                $announcement);
            $announcement = str_replace('<span class="break">&nbsp;</span>', '', $announcement);
            $announcement = preg_replace('/(listeitemNummer[a-zA-Z0-9 \_]*">.*)</mU', "\$1\t<", $announcement);
            $crawler = new Crawler($announcement);
            $objAnn->title = trim($crawler->filter('.metaTitleText')->first()->text('', true));

            $meta = $crawler
                ->filter('table.meta tr')
                ->each(function (Crawler $node, $i) {
                    return array(
                        trim($node->filter('th')->first()->text('', true)),
                        trim($node->filter('td')->first()->text('', true))
                    );
                });
            foreach ($meta as $row) {
                $objAnn->{strtolower($row[0])} = $row[1];
            }

            if ($cacheFolder == 'LTI-lov') {
                // :: Pick up text, item by item
                // Do a consistency check that we have all by comparing the text.
                $mainText = $crawler
                    ->filter('#documentBody')
                    ->each(function (Crawler $node, $i) {
                        return array(
                            'html' => $node->html(),
                            'text' => $node->text('', true)
                        );
                    });
                $mainText2 = $crawler
                    ->filter(
                        '#documentBody p, '
                        . '#documentBody table, '
                        // Paragraf without span inside (get these separately)
                        . '#documentBody div.paragraf, '
                        . '#documentBody div.paragraf span.paragrafValue, '
                        . '#documentBody div.paragraf span.paragrafTittel, '
                        . '#documentBody div.kapittel h2, '
                        . '#documentBody div.kapittel h3, '
                        . '#documentBody div.kapittel h4, '
                        . '#documentBody div.kapittel h5, '
                        . '#documentBody div.kapittel h6'
                    )
                    ->each(function (Crawler $node, $i) {
                        if ($node->nodeName() == 'div' && str_contains($node->attr('class'), 'paragraf')) {
                            $html = $node->html();
                            if (str_contains($html, '<span')) {
                                return null;
                            }
                        }
                        $cssClass = ' ' . $node->attr('class') . ' ';
                        $cssClass = str_replace(' morTag_am ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_am1 ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_an ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_a ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_z ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_n ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_m ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_mf ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_pp ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_nn ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_nnn ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_na ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_nf ', ' ', $cssClass);
                        $cssClass = str_replace(' morTag_af ', ' ', $cssClass);
                        $cssClass = str_replace(' no-text-indent ', ' ', $cssClass);
                        $cssClass = str_replace(' display-only ', ' ', $cssClass);
                        $cssClass = str_replace(' leftMargin_1 ', ' ', $cssClass);
                        $cssClass = str_replace(' leftMargin_2 ', ' ', $cssClass);
                        $cssClass = str_replace(' leftMargin_3 ', ' ', $cssClass);
                        $cssClass = str_replace(' center ', ' ', $cssClass);
                        $cssClass = str_replace(' small ', ' ', $cssClass);
                        $cssClass = str_replace(' margin_1 ', ' ', $cssClass);
                        $cssClass = str_replace(' body_margin_1 ', ' ', $cssClass);
                        $cssClass = str_replace(' body_border_1 ', ' ', $cssClass);
                        $cssClass = str_replace(' border_1 ', ' ', $cssClass);
                        $cssClass = str_replace(' font_ ', ' ', $cssClass);
                        $cssClass = str_replace(' morTableWidthPercent95 ', ' ', $cssClass);
                        $cssClass = str_replace(' morTableWidthPercent67 ', ' ', $cssClass);
                        $cssClass = str_replace('  ', ' ', $cssClass);
                        $cssClass = str_replace('  ', ' ', $cssClass);
                        $cssClass = str_replace('  ', ' ', $cssClass);
                        $cssClass = trim($cssClass);

                        return array(
                            'tag' => $node->nodeName(),
                            'class' => $cssClass,
                            'html' => $node->html(),
                            'text' => $node->text('', false)
                        );
                    });

                $mainText2_text = '';
                foreach ($mainText2 as $item) {
                    $mainText2_text .= ' ' . $item['text'];
                }

                $cleanText1 = str_replace(' ', '', $mainText[0]['text']);
                $cleanText2 = str_replace(' ', '', $mainText2_text);
                $cleanText1 = str_replace(' ', '', $cleanText1);
                $cleanText2 = str_replace(' ', '', $cleanText2);
                $cleanText2 = str_replace("\t", '', $cleanText2);
                if ($cleanText1 != $cleanText2) {
                    file_put_contents(__DIR__ . '/tmptmp-1',
                        '------------' . chr(10)
                        . '  #documentBody->text()' . chr(10)
                        . '------------' . chr(10)
                        . $cleanText1);
                    file_put_contents(__DIR__ . '/tmptmp-2',
                        '------------' . chr(10)
                        . '  Algo for picking up bit by by' . chr(10)
                        . '------------' . chr(10)
                        . $cleanText2);

                    echo "\n\n" . $objAnn->url . "\n\n";
                    throw new Exception('Not all text are picked up.');
                }
                elseif ($cacheFolder == 'LTI-lov') {
                    file_put_contents(
                        $announcementCacheFile . '.extracted_text.json',
                        json_encode($mainText2, JSON_PRETTY_PRINT ^ JSON_UNESCAPED_UNICODE ^ JSON_UNESCAPED_SLASHES)
                    );
                }

                $lawText = new stdClass();
                $lawText->lawId = $objAnn->dato;
                $lawText->lawName = str_strip_if_ends_with($objAnn->title, '.');
                $lawText->shortName = $objAnn->korttittel;
                $lawText->authorityDescription = null;
                $lawText->chapters = array();

                $chapter_found = false;
                $reading_error = false;
                $current_chapter = new stdClass();
                $current_chapter->paragraphs = array();
                $current_paragraph = null;
                foreach ($mainText2 as $item) {
                    if ($item == null) {
                        continue;
                    }

                    if ($item['tag'] == 'p'
                        && $item['class'] == 'marg'
                        && (str_starts_with($item['text'], 'Hjemmel: ') || str_starts_with($item['text'], 'Heimel: '))
                    ) {
                        $lawText->authorityDescription = $item['text'];
                    }
                    elseif (
                        $item['class'] == ''
                        && (
                            $item['tag'] == 'h2'
                            || $item['tag'] == 'h3'
                            || $item['tag'] == 'h4'
                            || $item['tag'] == 'h5'
                            || $item['tag'] == 'h6'
                        )
                    ) {
                        $chapter_found = true;
                        $current_chapter = new stdClass();
                        $current_chapter->name = $item['text'];
                        $current_chapter->paragraphs = array();
                        $lawText->chapters[] = $current_chapter;
                    }
                    elseif (
                        $item['class'] == 'paragraf'
                    ) {
                        // § 2a. <em class=" ">Unntak fra karanteneplikt ved utreise fra Norge</em>
                        $item['text'] = explode('<em class=" ">', $item['html']);

                        if (!isset($item['text'][1])) {
                            // § 1. Med skytevåpen forstås i denne lov:
                            $item['text'] = explode('. ', $item['html'], 2);
                        }
                        if (!isset($item['text'][1])) {
                            var_dump($item);
                            $item['text'][1] = '';
                        }

                        $item['text'][0] = trim($item['text'][0]);
                        $item['text'][1] = trim(str_replace('</em>', '', $item['text'][1]));
                        if (
                            str_contains($item['text'][0], '<')
                            || str_contains($item['text'][1], '<')
                        ) {
                            $item['text'][0] = strip_tags($item['text'][0]);
                            $item['text'][1] = strip_tags($item['text'][1]);
                            //var_dump($item);
                            //throw new Exception('Bogus value.');
                        }

                        $item['text'][0] = trim($item['text'][0]);
                        $item['text'][1] = trim($item['text'][1]);

                        $current_paragraph = new stdClass();
                        $current_paragraph->name = str_strip_if_ends_with($item['text'][0], '.');
                        $current_paragraph->title = $item['text'][1];
                        $current_paragraph->sections = array();
                        $current_chapter->paragraphs[] = $current_paragraph;
                    }
                    elseif (
                        $item['class'] == 'paragrafValue'
                    ) {
                        $current_paragraph = new stdClass();
                        $current_paragraph->name = str_strip_if_ends_with(trim($item['text']), '.');
                        $current_paragraph->title = null;
                        $current_paragraph->sections = array();
                        $current_chapter->paragraphs[] = $current_paragraph;
                    }
                    elseif (
                        $item['class'] == 'paragrafTittel'
                    ) {
                        $current_paragraph->title = trim($item['text']);
                    }
                    elseif (
                        $current_paragraph != null
                        && (
                            $item['class'] == 'numeral avsnitt'
                            || $item['class'] == 'avsnitt'
                            || ($item['tag'] == 'p' && $item['class'] == 'marg'
                                    && count($current_paragraph->sections) == 0
                            )
                        )
                    ) {
                        $current_paragraph->sections[] = $item['text'];
                    }
                    elseif (
                        $current_paragraph != null
                        && (
                            $item['class'] == 'listeItem avsnitt'
                        || ($item['tag'] == 'p' && $item['class'] == 'marg')
                        )
                    ) {
                        $current_paragraph->sections[count($current_paragraph->sections) - 1] .= "\n" . $item['text'];
                    }
                    elseif (
                        $current_paragraph == null
                        && (
                            ($item['tag'] == 'p' && $item['class'] == 'marg')
                            || $item['class'] == 'listeItem avsnitt'
                        )
                    ) {
                        // -> Not chapters yet. Add to authorityDescription.
                        $lawText->authorityDescription = trim($lawText->authorityDescription . "\n" . $item['text']);
                    }
                    else {
                        var_dump($lawText);
                        echo '----------' . chr(10);
                        echo "\n\n" . $objAnn->url . "\n\n";
                        echo $announcementCacheFile . '.extracted_text.json' . chr(10);
                        echo '----------' . chr(10);

                        if (isset($argv[1])) {
                            throw new Exception('Unknown item: ' . print_r($item, true));
                        }
                        $reading_error = true;
                    }
                }
                if (!$chapter_found && count($current_chapter->paragraphs) > 0) {
                    $lawText->chapters[] = $current_chapter;
                }
                if (!$reading_error && $cacheFolder == 'LTI-lov') {
                    file_put_contents(
                        __DIR__ . '/norway-law-java/src/main/resources/laws/' . $year . '/' . $cacheHtmlName . '.json',
                        json_encode($lawText, JSON_PRETTY_PRINT ^ JSON_UNESCAPED_UNICODE ^ JSON_UNESCAPED_SLASHES)
                    );
                }
            }

            if ($cacheFolder == 'LTI-forskrift') {
                $objAnn->type = 'forskrift';
                $objYear->itemsNationalRegulation[] = $objAnn;
                $obj->itemCountNationalRegulations++;
            }
            elseif ($cacheFolder == 'LTI-lov') {
                $objAnn->type = 'lov';
                $objYear->itemsLaw[] = $objAnn;
                $obj->itemCountLaws++;
            }
            elseif ($cacheFolder == 'LTII-forskrift') {
                $objAnn->type = 'lokalForskrift';
                $objYear->itemsLocalRegulation[] = $objAnn;
                $obj->itemCountLocalRegulations++;
            }
            else {
                throw new Exception('Err. Unknown type: ' . $cacheFolder);
            }

			// Find XML link
			// <a href="/xml/LTI/nl-20191220-111.xml" target="blank"><img src="/resources/images/blue-document-node.png"/>XML-versjon</a>
			preg_match(
				'/<a href="([\/a-zA-Z\-0-9\.]*)" target="blank"><img src="\/resources\/images\/blue-document-node\.png"\/>XML-versjon<\/a>/',
				$announcement,
				$matches
			);
			if (isset($matches[1])) {
				// /xml/LTII/lf-20200204-0104.xml
				if (str_starts_with($matches[1], '/xml/LTI/nl-') && str_ends_with($matches[1], '.xml')) {
				}
				elseif (str_starts_with($matches[1], '/xml/LTI/sf-') && str_ends_with($matches[1], '.xml')) {
				}
				elseif (str_starts_with($matches[1], '/xml/LTII/lf-') && str_ends_with($matches[1], '.xml')) {
				}
				else {
					throw new Exception('Unknown XML location: ' . $matches[1]);
				}
				$xml = getUrlCachedUsingCurl(
					$cacheTimeSeconds,
					$cache_location . '/' . $year . '/' . $cacheFolder . '/' . $cacheHtmlName . '.xml',
					$lovdataNo . $matches[1]
				);
			}
			else {
                logInfo('XML link not found: ' . $announcementCacheFile);
			}
		}
	}
}

file_put_contents(__DIR__ . '/norsk-lovtidend.json', json_encode($obj, JSON_PRETTY_PRINT ^ JSON_UNESCAPED_SLASHES ^ JSON_UNESCAPED_UNICODE));


function readItems($html) {
	$crawler = new Crawler($html);

	// Viser 1 - 20 av 1611
	$pagingInfo = $crawler->filter('.footer-pagination p')->each(function (Crawler $node, $i) {
			return $node->text('', true);
		})[0];
	preg_match('/Viser ([0-9]*) \- ([0-9]*) av ([0-9]*)/', $pagingInfo, $matches);

	$links1 = $crawler->filter('.documentList a')->each(function (Crawler $node, $i) {
			return array(
				'href' => $node->attr('href'),
				'text' => $node->text('', true)
			);
		});

	$links = array();
	foreach ($links1 as $link) {
		if (trim($link['text']) == '' && trim($link['href']) == '') {
			continue;
		}
		if (str_starts_with($link['text'], 'Kunngjort ') && str_starts_with($link['href'], '/register/lovtidend?kunngjortDato=')) {
			continue;
		}
		// /static/lovtidend/ltavd1/2020/sf-20200124-0078.pdf
		// /static/lovtidend/ltavd1/2020/sf-20200124-0078.pdf.asc
		if (trim($link['text']) == ''
			&& str_starts_with($link['href'], '/static/lovtidend/')
			&& (str_ends_with($link['href'], '.pdf') || str_ends_with($link['href'], '.pdf.asc'))
		) {
			continue;
		}
		$links[] = $link;
	}

	return array(
		'pages' => $pagingInfo,
		'item1' => $matches[1],
		'itemN' => $matches[2],
		'lastItem' => $matches[3],
		'links' => $links
	);
}

function getUrlCachedUsingCurl($cacheTimeSeconds, $cache_file, $baseUri, $acceptContentType = '') {
    if (file_exists($cache_file) && (time() - filemtime($cache_file)) < $cacheTimeSeconds) {
        return file_get_contents($cache_file);
    }
    logInfo('   - GET ' . $baseUri);
    $ci = curl_init();
    curl_setopt($ci, CURLOPT_URL, $baseUri);
    curl_setopt($ci, CURLOPT_TIMEOUT, 200);
    curl_setopt($ci, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ci, CURLOPT_FORBID_REUSE, 0);
    curl_setopt($ci, CURLOPT_CUSTOMREQUEST, 'GET');
    curl_setopt($ci, CURLOPT_HEADER, 1);
    $headers = array(
    );
    if ($acceptContentType != '') {
        $headers[] = 'Accept: ' . $acceptContentType;
    }
    curl_setopt($ci, CURLOPT_HTTPHEADER, $headers);
    $response = curl_exec($ci);
    if ($response === false) {
        throw new Exception(curl_error($ci), curl_errno($ci));
    }

    $header_size = curl_getinfo($ci, CURLINFO_HEADER_SIZE);
    $header = substr($response, 0, $header_size);
    $body = substr($response, $header_size);
    curl_close($ci);

    logInfo('   Response size: ' . strlen($body));

    if (!str_starts_with($header, 'HTTP/1.1 200 OK')) {
        if (str_starts_with($header, 'HTTP/1.1 404 Not Found') && file_exists($cache_file)) {
            logInfo('  -> 404 Not Found. Using cache.');
            return file_get_contents($cache_file);
        }
        logInfo('--------------------------------------------------------------' . chr(10)
            . $body . chr(10) . chr(10)
            . '--------------------------------------------------------------' . chr(10)
            . $header . chr(10) . chr(10)
            . '--------------------------------------------------------------');
        throw new Exception('Server did not respond with 200 OK.' . chr(10)
            . 'URL ...... : ' . $baseUri . chr(10)
            . 'Status ... : ' . explode(chr(10), $header)[0]
        );
    }

    if (trim($body) == '') {
        throw new Exception('Empty response.');
    }

    // These will change all the time. Remove
    // <link href="/resources/css/bootstrap.css?20-02-09-2112" rel="stylesheet">
    $body = preg_replace('/\s*<link href="[a-zA-Z\.\/\-0-9?\&]*" rel="stylesheet">\s*\n*/', '', $body);
    // <script src="/resources/js/json2.js?20-02-05-1326"></script>
    $body = preg_replace('/\s*<script src="[a-zA-Z\.\/\-0-9?\&]*"><\/script>\s*\n*/', '', $body);

    file_put_contents($cache_file, $body);
    return $body;
}

function str_starts_with($haystack, $needle) {
    return substr($haystack, 0, strlen($needle)) == $needle;
}

function str_ends_with($haystack, $needle) {
    $length = strlen($needle);
    return $length === 0 || substr($haystack, -$length) === $needle;
}

function str_contains($stack, $needle) {
    return (strpos($stack, $needle) !== FALSE);
}

function str_strip_if_ends_with($stack, $needle) {
    if (str_ends_with($stack, $needle)) {
        return substr($stack, 0, strlen($stack) - strlen($needle));
    }
    return $stack;
}

function logDebug($string) {
    //logLine($string, 'DEBUG');
}

function logInfo($string) {
    logLine($string, 'INFO');
}

function logError($string) {
    logLine($string, 'ERROR');
}

function logLine($string, $log_level) {
    global $run_key;
    echo date('Y-m-d H:i:s') . ' ' . $log_level . ' --- ' . $string . chr(10);

    if (isset($run_key) && !empty($run_key)) {
        // -> Download runner
        global $entity, $argv, $download_logs_directory;
        global $last_method;
        $line = new stdClass();
        $line->timestamp = time();
        $line->level = $log_level;
        $line->downloader = $argv[2];
        if (isset($entity) && isset($entity->entityId)) {
            $line->entity_id = $entity->entityId;
        }
        $line->last_method = $last_method;
        $line->message = $string;
        // Disabled.
        //file_put_contents($download_logs_directory . '/' . $run_key . '.json', json_encode($line) . chr(10), FILE_APPEND);
    }
}


function mkdirIfNotExists($dir) {
    if(!file_exists($dir)) {
        mkdir($dir, 0777, true);
    }
}


function htmlHeading($title = 'Norsk Lovtidend - Norske lover og forskrifter') {
    return "<!DOCTYPE html>
<html>
<head>
  <meta charset=\"UTF-8\">
  <title>$title</title>
</head>
<body>
<style>
table th {
	text-align: left;
	max-width: 300px;
	border: 1px solid lightgrey;
	padding: 2px;
	white-space: nowrap;
}
table td {
	text-align: left;
	border: 1px solid lightgrey;
	padding: 2px;
	white-space: nowrap;
	max-width: 700px;
	overflow: auto;
}
table {
	border-collapse: collapse;
}
</style>";
}

$htmlCreditLine = "Laget av <a href='https://twitter.com/hallny'>@hallny</a> / <a href='https://norske-postlister.no'>Norske-postlister.no</a><br>\n";
$htmlCreditLine .= "<a href='https://github.com/HNygard/norsk-lovtidend/'>Kildekode for oppdatering av denne lista</a> (Github)<br><br>\n\n";

$htmlMenu = '

<span style="font-size: 1.5em;">
Meny:
<a href="./">Hovedside</a>
-:- <a href="./lov.html">Til kunngjøringer om lover</a>
-:- <a href="./forskrift.html">Til kunngjøringer om forskrifter</a>
-:- <a href="./lokalForskrift.html">Til kunngjøringer om lokale forskrifter</a>
-:-
</span><br><br>

';

$html = htmlHeading() . "

<h1>Norske lover og forskrifter basert på Norsk Lovtidend</h1>\n";
$html .= $htmlCreditLine;
$html .= $htmlMenu;
$html .= '

<ul>
	<li>Antall nasjonale lover: ' . $obj->itemCountLaws . '</li>
	<li>Antall nasjonale forskrifter: ' . $obj->itemCountNationalRegulations . '</li>
	<li>Antall lokale forskrifter: ' . $obj->itemCountLocalRegulations . '</li>
	<li>Liste sist oppdatert: ' . $updateDate . '</li>
	<li>Kilde: <a href="' . $baseUrl . '">' . $baseUrl . '</a></li>
	<li>JSON-format: <a href="./norsk-lovtidend.json">norsk-lovtidend.json</a> (JSON-SIZE)</li>
	<li>CSV-format (Excel): 
        <ul>
            <li><a href="./norsk-lovtidend---lov.csv">norsk-lovtidend---lov.csv</a> (CSV-lov-SIZE)</li>
            <li><a href="./norsk-lovtidend---forskrift.csv">norsk-lovtidend---forskrift.csv</a> (CSV-forskrift-SIZE)</li>
            <li><a href="./norsk-lovtidend---lokalForskrift.csv">norsk-lovtidend---lokalForskrift.csv</a> (CSV-lokalForskrift-SIZE)</li>
        </ul>
    </li>
</ul>


';

$columns = array(
    'kunngjort' => 0,
    'dato' => 1,
    'korttittel' => 2
);

$announcementsCsv = array(
    'lov' => array(),
    'forskrift' => array(),
    'lokalForskrift' => array()
);
$announcementsHtml = array(
    'lov' => array(),
    'forskrift' => array(),
    'lokalForskrift' => array()
);
$heading = array(
    'lov' => 'Kunngjøringer om nye lover i Norsk Lovtidend',
    'forskrift' => 'Kunngjøringer om nye forskrifter i Norsk Lovtidend',
    'lokalForskrift' => 'Kunngjøringer om nye lokale forskrifter i Norsk Lovtidend'
);

$csv = '';
foreach ($obj->announcementsPerYear as $year => $objYear) {
    $items = array_merge(
        $objYear->itemsLaw,
        $objYear->itemsLocalRegulation,
        $objYear->itemsNationalRegulation
    );
    foreach ($items as $item) {
        $itemArray = (array) $item;
        foreach ($itemArray as $key => $value) {
            if (!isset($columns[$key])) {
                $columns[$key] = count($columns);
            }
        }

        $csv = '';
        foreach ($columns as $column => $i) {
            if (isset($itemArray[$column])) {
                $csv .= str_replace(';', ':', $itemArray[$column]);
            }
            else {
                $itemArray[$column] = '';
            }
            $csv .= ';';
        }
        $csv .= "\n";
        $announcementsCsv[$itemArray['type']][] = $csv;

        $announcementsHtml[$itemArray['type']][] = '
	<tr>
		<th>' . $itemArray['journalnr'] . '</th>
		<th>' . $itemArray['kunngjort']
            . ((isset($itemArray['publisert']) && !empty($itemArray['publisert'])) ? ' <span style="font-weight: normal;">(' . $itemArray['publisert'] . ')</span>' : '') . '</th>
		<td>[<a href="' . $itemArray['url'] . '">Til lov/forskrift</a>]</td>
		<td>' . $itemArray['korttittel'] . '</td>
		<td>' . $itemArray['title'] . '</td>
	</tr>
';
    }
}
foreach ($announcementsCsv as $type => $csvs) {
    $csv = "Datasett hentet fra;https://hnygard.github.io/norsk-lovtidend/;@hallny / Norske-postlister.no;Kilde;$baseUrl;Data hentet;$updateDate\n";
    $csv .= "\n";
    foreach ($columns as $column => $i) {
        $csv .= str_replace(';', ':', $column) . ';';
    }
    $csv .= "\n" . implode('', $csvs);
    file_put_contents(__DIR__ . '/norsk-lovtidend---' . $type . '.csv', $csv);
    $html = str_replace('CSV-' . $type . '-SIZE', human_filesize(filesize(__DIR__ . '/norsk-lovtidend---' . $type . '.csv')), $html);

}
foreach ($announcementsHtml as $type => $htmls) {
    $html2 = htmlHeading() . '
<h1>' . $heading[$type] . ' </h1>
' . $htmlCreditLine . '
' . $htmlMenu . '

<a href="./norsk-lovtidend---' . $type . '.csv">Last ned som CSV</a> (Excel)<br><br>

<table>
	<thead>
		<tr>
            <th>Journalnr</th>
			<th>Kunngjort/publisert</th>
			<th>Lovdata.no</th>
			<th>Korttittel</th>
			<th>Tittel</th>
		</tr>
	</thead>

' . implode('', $htmls) . '

</table>
';

    file_put_contents(__DIR__ . '/' . $type . '.html', $html2);
}


function human_filesize($size, $precision = 2) {
    $units = array('B', 'kB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB');
    $step = 1024;
    $i = 0;
    while (($size / $step) > 0.9) {
        $size = $size / $step;
        $i++;
    }
    return number_format($size, $precision, ',', ' ') . ' ' . $units[$i];
}

$html = str_replace('JSON-SIZE', human_filesize(filesize(__DIR__ . '/norsk-lovtidend.json')), $html);

file_put_contents(__DIR__ . '/index.html', $html);

