import {Jwt} from "./JwtDto.ts";
import {Error} from "@mui/icons-material";
import {jwtDecode} from "jwt-decode";
import {fromUnixTime} from "date-fns/fromUnixTime";

export class AuthenticationHelper {
    private constructor() {
    }

    public static fromJSON(object: any): Authentication {
        if (object.__type === NoAuthentication.name) {
            return NoAuthentication.instance()
        } else if (object.__type === TokenAuthentication.name) {
            let token = object.token
            return new TokenAuthentication(token)
        } else {
            throw Error
        }
    }
}

export interface Authentication {
    name(): string

    email(): string

    subject(): string

    roles(): string[]

    expiry(): Date
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

    name(): string {
        return "";
    }

    roles(): string[] {
        return [];
    }

    email(): string {
        // @ts-ignore
        throw new Error('NoAuthentication - there is no email')
    }

    subject(): string {
        // @ts-ignore
        throw new Error('NoAuthentication - there is no subject')
    }

    expiry(): Date {
        // @ts-ignore
        throw new Error('NoAuthentication - there is no expiry')
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

export class TokenAuthentication implements Authentication {
    readonly token: string

    private readonly _name: string
    private readonly _email: string
    private readonly _roles: Role[]
    private readonly _subject: string
    private readonly _expiry: Date

    constructor(token: string) {
        this.token = token

        let jwt = jwtDecode<Jwt>(token)

        this._name = jwt.name
        this._email = jwt.email
        this._subject = jwt.sub
        this._expiry = fromUnixTime(jwt.exp)

        function enumFromStringValue<T> (enm: { [s: string]: T}, value: string): T | undefined {
            return (Object.values(enm) as unknown as string[]).includes(value)
                ? value as unknown as T
                : undefined
        }

        this._roles = jwt.realm_access.roles
            .map(role => enumFromStringValue(Role, role))
            .filter(role => role != undefined) as Role[]
    }

    roles(): Role[] {
        return this._roles
    }

    name(): string {
        return this._name
    }

    email(): string {
        return this._email
    }

    subject(): string {
        return this._subject
    }

    expiry(): Date {
        return this._expiry
    }

    public toJSON() {
        return {
            __type: TokenAuthentication.name,
            token: this.token,
        }
    }
}

export enum Role {
    EMPLOYER_CALENDAR = "employer calendar",
    TEMPLATE_ADMIN = "template admin",
    BOOKING_ADMIN = "booking admin",
    SHIFT_ADMIN = "shift admin",
}
