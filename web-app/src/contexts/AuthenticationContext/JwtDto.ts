export interface Jwt {
    name: string,
    email: string,
    sub: string,
    iss: string,
    exp: number,
    realm_access: JwtRealmAccess,
}

export interface JwtRealmAccess {
    roles: string[],
}
