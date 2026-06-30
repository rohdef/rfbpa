import {Data} from "dataclass";
import {Booking} from "./Booking.ts"

export class Shift extends Data {
    shiftId: string = ""
    start: Date = new Date()
    end: Date = new Date()
    booking: Booking = ""
}
