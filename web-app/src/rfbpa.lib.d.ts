export {}
declare global {
    interface Window {
        _env_: {
            apiUrl: string,

            auth: {
                url: string,
                client: string,
            }
        };
    }
}
