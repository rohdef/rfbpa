export {}
declare global {
    interface Window {
        _env_: {
            apiUrl: string,

            auth: {
                url: string,
                realm: string,
                client: string
            }
        };
    }
}
