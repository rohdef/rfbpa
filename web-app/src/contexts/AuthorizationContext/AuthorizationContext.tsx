import {ReactNode} from "react";
import {useAuthentication} from "../AuthenticationContext/AuthenticationContext";
import configuration from "../../configuration.ts";
import {Authentication, NoAuthentication} from "../AuthenticationContext/Authentication.tsx";

enum Authorization {
    AUTHORIZED = "authorized",
    NOT_LOGGED_IN = "not_loggede_in"
}

function checkLogin({authentication}: { authentication: Authentication }): Authorization {
    if (authentication instanceof NoAuthentication) {
        return Authorization.NOT_LOGGED_IN
    }
    // if (allowedRoles.filter(role => user.roles.contains(role))) {
    //     return Authorization.AUTHORIZED
    // }
    // return Authorization.ROLE_MISSING
    return Authorization.AUTHORIZED
}

const AuthorizationContext = ({children}: { children: ReactNode }) => {
    const {authentication} = useAuthentication()
    const authorization = checkLogin({authentication})

    switch (authorization) {
        case Authorization.NOT_LOGGED_IN:
            const authUrl = configuration.auth.url
            const client = configuration.auth.client
            const callbackUrl = `${window.location.href}/`
            const redirect = `redirect_uri=${encodeURIComponent(callbackUrl)}`;
            const responseTyep = `response_type=token`;
            const clientId = `client_id=${client}`;
            const scope = `scope=openid%20email%20profile`;
            const targetUrl = `${authUrl}?${redirect}&${responseTyep}&${clientId}&${scope}`
            window.location.href = targetUrl
            // should never happen due to redirect above
            return null
        case Authorization.AUTHORIZED:
            return children
    }
}

export default AuthorizationContext
