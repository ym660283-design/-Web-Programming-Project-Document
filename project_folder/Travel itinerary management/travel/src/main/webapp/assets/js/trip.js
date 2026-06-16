(function () {
    function initializeScheduleDateSelection() {
        const scheduleDateSelect = document.querySelector("[data-schedule-date-select]");
        const manageList = document.querySelector("[data-schedule-manage-list]");

        if (!scheduleDateSelect || !manageList) {
            return;
        }

        function updateScheduleManageList() {
            const selectedOption = scheduleDateSelect.options[scheduleDateSelect.selectedIndex];
            const selectedDay = selectedOption ? selectedOption.getAttribute("data-day") : "";
            const selectedPanel = manageList.querySelector(
                "[data-schedule-day-panel='" + selectedDay + "']"
            );
            const heading = manageList.querySelector("[data-schedule-manage-heading]");
            const count = manageList.querySelector("[data-schedule-manage-count]");

            if (!selectedDay || !selectedPanel || !heading || !count) {
                return;
            }

            manageList.querySelectorAll("[data-schedule-day-panel]").forEach(function (panel) {
                panel.hidden = panel !== selectedPanel;
            });

            heading.textContent = "DAY " + selectedDay + " 등록 일정 관리";
            count.textContent = selectedPanel.getAttribute("data-schedule-count") + "개 일정";
        }

        scheduleDateSelect.addEventListener("change", updateScheduleManageList);
        scheduleDateSelect.form.addEventListener("reset", function () {
            window.setTimeout(updateScheduleManageList, 0);
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        initializeScheduleDateSelection();

        if (!window.TravelMap) {
            return;
        }

        window.TravelMap.getKakaoApi(function (apiReady) {
            window.TravelMap.initializeTripMap(apiReady);
            window.TravelMap.initializeLocationSearch(apiReady);
        });
    });
})();
