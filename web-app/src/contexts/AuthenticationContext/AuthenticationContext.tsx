import React, {ReactNode, useEffect, useState} from "react"
import {Data} from "dataclass"

export class AuthenticationHelper {
    private constructor() {
    }

    public static fromJSON(object: any): Authentication {
        if (object.__type === NoAuthentication.name) {
            return NoAuthentication.instance()
        } else if (object.__type === TokenAuthentication.name) {
            delete object.__type
            return TokenAuthentication.create(object)
        } else {
            throw Error
        }
    }
}

export interface Authentication {
    email(): string
    subject(): string
    roles(): string[]
}

export class NoAuthentication implements Authentication {
    private static _instance: Authentication

    private constructor() {
    }

    public static instance() {
        if (!NoAuthentication._instance) {
            NoAuthentication._instance = new NoAuthentication()
        }
        return NoAuthentication._instance
    }

    roles(): string[] {
        return [];
    }

    email(): string {
        throw new Error('NoAuthentication - there is no email')
    }

    subject(): string {
        throw new Error('NoAuthentication - there is no subject')
    }

    public toJSON() {
        return {
            __type: NoAuthentication.name,
        }
    }

    public toString() {
        return JSON.stringify(this.toJSON())
    }
}

export class TokenAuthentication extends Data implements Authentication {
    token: string = ""

    roles(): string[] {
        return [];
    }

    email(): string {
        return ""
    }

    subject(): string {
        return ""
    }

    public toJSON() {
        return {
            __type: TokenAuthentication.name,
            token: this.token,
        }
    }
}

interface AuthenticationValues {
    authentication: Authentication
    setAuthentication: (authentication: Authentication) => void,
    resetAuthentication: () => void,
}

export const AuthenticationContext = React.createContext<AuthenticationValues>(
    {
        authentication: NoAuthentication.instance(),
        setAuthentication: (authentication: Authentication) => {
        },
        resetAuthentication: () => {
        },
    },
)

// TODO check up on type for children, hopefully it's fine for now
export function AuthenticationProvider({children}: { children: ReactNode }) {
    const [authentication, setAuthentication] = useState<Authentication>(() => {
        const accessTokenRegex = /[?&#]access_token=([^&]+)/;

        const matches = window.location.href.match(accessTokenRegex);

        if (matches) {
            const authentication1 = TokenAuthentication.create({
                token: matches[1],
            })

            localStorage.setItem("authentication", JSON.stringify(authentication1))
            return authentication1
        }

        const initialAuth = localStorage.getItem("authentication")
        if (initialAuth) {
            return AuthenticationHelper.fromJSON(JSON.parse(initialAuth))
        } else {
            return NoAuthentication.instance()
        }
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
