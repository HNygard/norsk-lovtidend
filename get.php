<?php
/**
 * This script downloads all announcements in Norsk Lovtidend, released by Ministry of Justice, published by Lovdata.
 *
 * @author Hallvard Nygård, @hallny / Norske-postlister.no.
 */

set_error_handler(function ($errno, $errstr, $errfile, $errline, array $errcontext) {
    throw new ErrorException($errstr, 0, $errno, $errfile, $errline);
});

require __DIR__ . '/vendor/autoload.php';
use Symfony\Component\DomCrawler\Crawler;

$cacheTimeSecondsThisYear = 60 * 60 * 24 * 4;
$cacheTimeSecondsPrevYears = 60 * 60 * 24 * 365;
$cache_location = __DIR__ . '/cache';
$lovdataNo = 'https://lovdata.no';
$baseUrl = $lovdataNo . '/register/lovtidend';
$updateDate = date('d.m.Y H:i:s');

$lawsNotPresent = array();

$obj = new stdClass();
$obj->lastUpdated = $updateDate;
$obj->sourceInfo = "Datasett hentet fra https://hnygard.github.io/norsk-lovtidend/, laget av @hallny. Kilde: $baseUrl";
$obj->announcementsPerYear = array();

for($year = date('Y'); $year >= 2020; $year--) {
    $objYear = new stdClass();
    $obj->announcementsPerYear[] = $objYear;
    $objYear->pageCount = 1;
    $objYear->itemCount = 0;
    $objYear->itemsNationalLaw = array();
    $objYear->itemsLocalRegulation = array();
    $objYear->itemsNationalRegulation = array();

	$cacheTimeSeconds = date('Y') == $year ? $cacheTimeSecondsThisYear : $cacheTimeSecondsPrevYears;
	mkdirIfNotExists($cache_location . '/' . $year . '/paged-list');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTI-lov');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTI-forskrift');
	mkdirIfNotExists($cache_location . '/' . $year . '/LTII-forskrift');
	$offset = 0;
	$maxOffset = 10;
	while ($offset <= $maxOffset) {
		$mainPage = getUrlCachedUsingCurl(
			$cacheTimeSeconds,
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

            $objAnn = new stdClass();
            $objAnn->url = $lovdataNo . $link['href'];

			$announcementCacheFile = $cache_location . '/' . $year . '/' . $cacheFolder . '/' . $cacheHtmlName . '.html';
			$announcement = getUrlCachedUsingCurl(
				$cacheTimeSeconds,
				$announcementCacheFile,
				$lovdataNo . $link['href']
			);
            $objYear->itemCount++;

            $crawler = new Crawler($announcement);
            $objAnn->title = trim($crawler->filter('.metaTitleText')->first()->text());

            if ($cacheFolder == 'LTI-forskrift') {
                $objYear->itemsNationalRegulation[] = $objAnn;
            }
            elseif ($cacheFolder == 'LTI-lov') {
                $objYear->itemsNationalLaw[] = $objAnn;
            }
            elseif ($cacheFolder == 'LTII-forskrift') {
                $objYear->itemsLocalRegulation[] = $objAnn;
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



