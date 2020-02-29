var searchBox = document.getElementById('law-ref-search');
var currentSearch = null;
var search = function() {
    var value = searchBox.value;
    if (value !== currentSearch) {
        currentSearch = value;
        fetch('./api/law-reference?searchQuery=' + value)
            .then((response) => {
                return response.json();
            })
            .then((data) => {
                console.log(data);
                if (data.status && data.status === 500) {
                    document.getElementById('law-ref-result-info').innerHTML = '';
                    document.getElementById("law-ref-result").innerHTML = "<div class='alert alert-danger'><b>" + data.message + "</b><br><br>" +
                        "<pre>" + data.trace + "</pre></div>";
                } else {
                    var resultInfo = '<b>Match found:</b> ' + data.lawReference + '&nbsp;&nbsp;&nbsp;<b>Match type:</b>';
                    data.lawReferenceMatchTypes.forEach((matchType) => {
                        resultInfo += ' ' + matchType;
                    });
                    document.getElementById('law-ref-result-info').innerHTML = resultInfo;
                    document.getElementById("law-ref-result").innerHTML = "<div class='law-ref-result-success'>" + data.html + "</div>";
                }
            });
    }
};

searchBox.onkeyup = search;
searchBox.onchange = search;
