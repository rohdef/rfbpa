import {Data} from "dataclass"

export interface Booking {
}

export class NoBooking implements Booking {
    private static _instance: NoBooking

    private constructor() {}

    public static get instance(): NoBooking {
        if (!this._instance) {
            this._instance = new NoBooking()
        }
        return this._instance
    }
}

export class HelperBooking extends Data implements Booking {
    id: string = ""
}