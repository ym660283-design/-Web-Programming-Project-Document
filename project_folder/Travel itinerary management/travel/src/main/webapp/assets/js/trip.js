(function () {
    document.addEventListener("DOMContentLoaded", function () {
        if (!window.TravelMap) {
            return;
        }

        window.TravelMap.getKakaoApi(function (apiReady) {
            window.TravelMap.initializeTripMap(apiReady);
            window.TravelMap.initializeLocationSearch(apiReady);
        });
    });
})();
