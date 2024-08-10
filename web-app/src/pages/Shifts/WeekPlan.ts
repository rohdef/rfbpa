import {Data} from "dataclass";
import {Shift} from "./Shift.ts";

export class WeekPlan extends Data {
    week: string = ""

    monday: Shift[] = []
    tuesday: Shift[] = []
    wednesday: Shift[] = []
    thursday: Shift[] = []
    friday: Shift[] = []
    saturday: Shift[] = []
    sunday: Shift[] = []

    allShifts(): Shift[] {
        return [
            ...this.monday,
            ...this.tuesday,
            ...this.wednesday,
            ...this.thursday,
            ...this.friday,
            ...this.saturday,
            ...this.sunday,
        ]
    }
}
