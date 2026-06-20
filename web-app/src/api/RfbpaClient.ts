import axios, { AxiosInstance, AxiosResponse } from "axios";
import {WeekPlan} from "../pages/Shifts/WeekPlan";
import {Shift} from "../pages/Shifts/Shift";
import {parseISO} from "date-fns";
import {TokenAuthentication} from "../contexts/AuthenticationContext/Authentication.tsx"
import {Helper} from "../helpers/Helper.ts"
import {HelperStorage} from "../helpers/HelperStorage.ts"

interface HelperBookingDto {
    type: string;
    name: string;
}

interface ShiftDto {
    shiftId: string;
    start: string;
    end: string;
    helperBooking: HelperBookingDto;
}

interface WeekPlanDto {
    week: string;
    monday: ShiftDto[];
    tuesday: ShiftDto[];
    wednesday: ShiftDto[];
    thursday: ShiftDto[];
    friday: ShiftDto[];
    saturday: ShiftDto[];
    sunday: ShiftDto[];
}

interface HelperDto {
    id: string;
    name: string;
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

    private toHelper(dto: HelperDto): Helper {
        return Helper.create({
            id: dto.id,
            name: dto.name,
        })
    }

    async getHelpers(token: TokenAuthentication): Promise<Helper[]> {
        const helpers = await this.client.get<HelperDto[]>(
            "/helpers",
            {
                headers: {
                    Authorization: `Bearer ${token.token}`,
                },
            },
        )

        return helpers.data.map((h) => this.toHelper(h))
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

    async reportIllness(shiftId: string, token: TokenAuthentication): Promise<Shift> {
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
        return this.toShift(response.data)
    }
}