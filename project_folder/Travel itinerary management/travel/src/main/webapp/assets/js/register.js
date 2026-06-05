(function () {
    var form = document.querySelector("#registerForm");

    if (!form) {
        return;
    }

    var loginId = form.querySelector("#loginId");
    var loginIdChecked = form.querySelector("#loginIdChecked");
    var checkedLoginId = form.querySelector("#checkedLoginId");
    var checkLoginButton = form.querySelector("#loginIdCheckButton");
    var loginIdCheckMessage = form.querySelector("#loginIdCheckMessage");
    var password = form.querySelector("#password");
    var passwordConfirm = form.querySelector("#passwordConfirm");
    var checkLoginUrl = form.getAttribute("data-check-login-url");

    if (!loginId || !loginIdChecked || !checkedLoginId || !checkLoginButton || !loginIdCheckMessage) {
        return;
    }

    function resetLoginIdCheck() {
        loginIdChecked.value = "false";
        checkedLoginId.value = "";
    }

    function showLoginIdMessage(message, className) {
        loginIdCheckMessage.className = ("form-text " + (className || "")).replace(/^\s+|\s+$/g, "");
        loginIdCheckMessage.textContent = message;
    }

    function validatePasswordConfirm() {
        if (!password || !passwordConfirm) {
            return;
        }

        var matches = password.value === passwordConfirm.value;
        passwordConfirm.setCustomValidity(matches ? "" : "비밀번호가 일치하지 않습니다.");
    }

    checkLoginButton.addEventListener("click", function () {
        var value = loginId.value.replace(/^\s+|\s+$/g, "");

        resetLoginIdCheck();
        showLoginIdMessage("아이디를 확인하는 중입니다.", "text-muted");

        var request = new XMLHttpRequest();
        request.open("GET", checkLoginUrl + "?login_id=" + encodeURIComponent(value), true);
        request.onreadystatechange = function () {
            if (request.readyState !== 4) {
                return;
            }

            if (request.status < 200 || request.status >= 300) {
                showLoginIdMessage("아이디 중복 확인 중 오류가 발생했습니다.", "text-danger");
                return;
            }

            var result;
            try {
                result = JSON.parse(request.responseText);
            } catch (error) {
                showLoginIdMessage("아이디 중복 확인 중 오류가 발생했습니다.", "text-danger");
                return;
            }

            if (result.available) {
                loginIdChecked.value = "true";
                checkedLoginId.value = value;
                showLoginIdMessage(result.message, "text-success");
            } else {
                showLoginIdMessage(result.message, "text-danger");
            }
        };
        request.send();
    });

    loginId.addEventListener("input", function () {
        resetLoginIdCheck();
        showLoginIdMessage("", "");
    });

    if (password && passwordConfirm) {
        password.addEventListener("input", validatePasswordConfirm);
        passwordConfirm.addEventListener("input", validatePasswordConfirm);
    }

    form.addEventListener("submit", function (event) {
        validatePasswordConfirm();

        if (loginIdChecked.value !== "true" || checkedLoginId.value !== loginId.value.replace(/^\s+|\s+$/g, "")) {
            event.preventDefault();
            event.stopPropagation();
            showLoginIdMessage("아이디 중복확인을 먼저 완료해주세요.", "text-danger");
            loginId.focus();
            return;
        }

        if (typeof form.checkValidity === "function" && !form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }

        form.classList.add("was-validated");
    });
})();
