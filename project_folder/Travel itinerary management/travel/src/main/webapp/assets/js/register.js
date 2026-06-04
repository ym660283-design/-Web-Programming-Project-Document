(() => {
    const form = document.querySelector("#registerForm");

    if (!form) {
        return;
    }

    const password = form.querySelector("#password");
    const passwordConfirm = form.querySelector("#passwordConfirm");

    const validatePasswordConfirm = () => {
        const matches = password.value === passwordConfirm.value;
        passwordConfirm.setCustomValidity(matches ? "" : "비밀번호가 일치하지 않습니다.");
    };

    password.addEventListener("input", validatePasswordConfirm);
    passwordConfirm.addEventListener("input", validatePasswordConfirm);

    form.addEventListener("submit", (event) => {
        validatePasswordConfirm();

        if (!form.checkValidity()) {
            event.preventDefault();
            event.stopPropagation();
        }

        form.classList.add("was-validated");
    });
})();
