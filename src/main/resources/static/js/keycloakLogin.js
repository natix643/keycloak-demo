function initKeycloak() {
    var keycloak = Keycloak();
    keycloak.onAuthLogout = renderAuth;
    keycloak.init()
        .success(renderAuth)
        .error(function () {
            alert('failed to initialize keycloak');
        });
    return keycloak;
}

function refresh() {
    keycloak.updateToken(30)
        .success(function (refreshed) {
            if (!refreshed) {
                alert('token still valid');
            }
            renderAuth();
        })
        .error(function () {
            alert('failed to refresh the access token')
        });
}

function renderAuth() {
    $('#username').text(keycloak.authenticated ? keycloak.tokenParsed.preferred_username : 'anonymous');
    $('#accessToken').text(keycloak.token || '');
    $('#refreshToken').text(keycloak.refreshToken || '');
}

function callApi() {
    $('#apiData').text('');
    $.get({
        url: '/api',
        headers: {'Authorization': 'Bearer ' + keycloak.token}
    }).done(function (data) {
        var json = JSON.stringify(data, null, '  ');
        $('#apiData').text(json);
    }).fail(function (jqXHR, textStatus) {
        $('#apiData').text(textStatus);
        console.error(jqXHR);
    });
}

$(document).ready(function () {
    window.keycloak = initKeycloak();

    $('#loginButton').click(keycloak.login);
    $('#logoutButton').click(keycloak.logout);
    $('#clearButton').click(keycloak.clearToken);
    $('#refreshButton').click(refresh);
    $('#apiButton').click(callApi);
});
