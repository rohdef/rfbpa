import axios, { AxiosInstance, AxiosResponse } from "axios";
import {WeekPlan} from "../pages/Shifts/WeekPlan";
import {Shift} from "../pages/Shifts/Shift";
import {parseISO} from "date-fns";
import {TokenAuthentication} from "../contexts/AuthenticationContext/Authentication.tsx"

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

export class RfbpaClient {
    private client: AxiosInstance;

    constructor(baseUrl: string) {
        this.client = axios.create({
            baseURL: baseUrl,
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

    async getShiftsInWeek(week: string, token: TokenAuthentication): Promise<WeekPlan> {
        const weekPlans = await this.getShiftsInWeekInterval(week, week, token)
        return weekPlans[0]
    }

    async getShiftsInWeekInterval(startWeek: string, endWeek: string, token: TokenAuthentication): Promise<WeekPlan[]> {
        const yearWeekInterval = `${startWeek}--${endWeek}`
        const response: AxiosResponse<WeekPlanDto[]> = await this.client.get(
            `shifts/in-interval/${yearWeekInterval}`,
            {
                headers: {
                    Authorization: `Bearer ${token.token}`,
                },
            }
        );
        return response.data.map((dto) => this.toWeekPlan(dto));
    }

    async updateShift(shift: Shift, token: TokenAuthentication): Promise<void> {
        console.log(`Stub: Updating shift ${shift.shiftId}`, shift);
        // await this.client.put(`shifts/${shift.shiftId}`, shift, {
        //     headers: {
        //         Authorization: `Bearer ${token}`,
        //     },
        // });
    }

    async deleteShift(shiftId: string, token: TokenAuthentication): Promise<void> {
        console.log(`Stub: Deleting shift ${shiftId}`);
        // await this.client.delete(`shifts/${shiftId}`, {
        //     headers: {
        //         Authorization: `Bearer ${token}`,
        //     },
        // });
    }

    async reportIllness(shiftId: string, token: TokenAuthentication): Promise<string> {
        console.log(`Reporting illness for shift ${shiftId}`);
        const response: AxiosResponse<ShiftDto> = await this.client.put(
            `shifts/${shiftId}/registrations/illness`,
            "",
            {
                headers: {
                    "Content-Type": "application/json",
                    Authorization: `Bearer ${token.token}`,
                },
            }
        );
        console.log("New shift:");
        console.log(response.data);
        return response.data.shiftId
    }
}
