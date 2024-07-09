import React, {ReactNode, useEffect, useState} from "react"
import {Authentication, AuthenticationHelper, NoAuthentication, TokenAuthentication} from "./Authentication.tsx";
import {isAfter} from "date-fns/isAfter";


interface AuthenticationValues {
    authentication: Authentication
    setAuthentication: (authentication: Authentication) => void,
    resetAuthentication: () => void,
}

// @ts-ignore
// @ts-ignore
export const AuthenticationContext = React.createContext<AuthenticationValues>(
    {
        authentication: NoAuthentication.instance(),
        // @ts-ignore
        setAuthentication: (authentication: Authentication) => {},
        resetAuthentication: () => {},
    },
)

// TODO check up on type for children, hopefully it's fine for now
export function AuthenticationProvider({children}: { children: ReactNode }) {
    const [authentication, setAuthentication] = useState<Authentication>(() => {
        const accessTokenRegex = /[?&#]access_token=([^&]+)/;

        const matches = window.location.href.match(accessTokenRegex);

        if (matches) {
            const freshAuthentication = new TokenAuthentication(matches[1])

            localStorage.setItem("authentication", JSON.stringify(freshAuthentication))
            return freshAuthentication
        }

        const currentAuthenticationSession = localStorage.getItem("authentication")
        if (currentAuthenticationSession) {
            const authentication = AuthenticationHelper.fromJSON(JSON.parse(currentAuthenticationSession))

            if (authentication instanceof TokenAuthentication) {

                const expiry = authentication.expiry()
                const now = Date.now()

                if (isAfter(expiry, now)) {
                    // TODO probaly a decent place for refresh logic?
                    return authentication
                } else {
                    console.error("Token is expired")
                }
            }
        }

        localStorage.removeItem("authentication")
        return NoAuthentication.instance()
    })

    useEffect(() => {
        localStorage.setItem("authentication", JSON.stringify(authentication))
    }, [authentication])

    return (
        <AuthenticationContext.Provider value={{
            authentication,
            setAuthentication,
            resetAuthentication: () => setAuthentication(NoAuthentication.instance())
        }}>
            {children}
        </AuthenticationContext.Provider>
    )
}

export function useAuthentication() {
    return React.useContext(AuthenticationContext)
}
