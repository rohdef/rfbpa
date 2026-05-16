import axios, { AxiosInstance, AxiosResponse } from "axios";
import {WeekPlan} from "../pages/Shifts/WeekPlan";
import {Shift} from "../pages/Shifts/Shift";
import {parseISO} from "date-fns";

export interface HelperBookingDto {
    type: string;
    name: string;
}

export interface ShiftDto {
    shiftId: string;
    start: string;
    end: string;
    helperBooking: HelperBookingDto;
}

export interface WeekPlanDto {
    week: string;
    monday: ShiftDto[];
    tuesday: ShiftDto[];
    wednesday: ShiftDto[];
    thursday: ShiftDto[];
    friday: ShiftDto[];
    saturday: ShiftDto[];
    sunday: ShiftDto[];
}

export class RfBpaClient {
    private client: AxiosInstance;

    constructor(token: string) {
        this.client = axios.create({
            baseURL: "http://localhost:8080/api/public",
            headers: {
                Authorization: `Bearer ${token}`,
            },
        });
    }

    private toShift(shift: ShiftDto): Shift {
        let helper: string;
        if (shift.helperBooking.type === "Booking") {
            helper = shift.helperBooking.name;
        } else {
            helper = "Ikke booket";
        }
        return Shift.create({
            shiftId: shift.shiftId,
            start: parseISO(shift.start),
            end: parseISO(shift.end),
            helper: helper,
        });
    }

    private toWeekPlan(dto: WeekPlanDto): WeekPlan {
        return WeekPlan.create({
            week: dto.week,
            monday: dto.monday.map((s) => this.toShift(s)),
            tuesday: dto.tuesday.map((s) => this.toShift(s)),
            wednesday: dto.wednesday.map((s) => this.toShift(s)),
            thursday: dto.thursday.map((s) => this.toShift(s)),
            friday: dto.friday.map((s) => this.toShift(s)),
            saturday: dto.saturday.map((s) => this.toShift(s)),
            sunday: dto.sunday.map((s) => this.toShift(s)),
        });
    }

    async getShiftsInWeek(yearWeekInterval: string): Promise<WeekPlan[]> {
        const response: AxiosResponse<WeekPlanDto[]> = await this.client.get(
            `shifts/in-interval/${yearWeekInterval}`
        );
        return response.data.map((dto) => this.toWeekPlan(dto));
    }

    async updateShift(shift: Shift): Promise<void> {
        console.log(`Stub: Updating shift ${shift.shiftId}`, shift);
        // await this.client.put(`shifts/${shift.shiftId}`, shift);
    }

    async deleteShift(shiftId: string): Promise<void> {
        console.log(`Stub: Deleting shift ${shiftId}`);
        // await this.client.delete(`shifts/${shiftId}`);
    }

    async reportIllness(shiftId: string): Promise<void> {
        console.log(`Stub: Reporting illness for shift ${shiftId}`);
        // await this.client.post(`shifts/${shiftId}/report-illness`);
    }
}
