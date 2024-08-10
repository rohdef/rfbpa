import {Data} from "dataclass";

export class Shift extends Data {
    shiftId: string = ""
    start: Date = new Date()
    end: Date = new Date()
    helper: string = ""
}
