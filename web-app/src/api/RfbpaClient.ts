import axios, {AxiosInstance, AxiosResponse} from "axios";
import {WeekPlan} from "../pages/Shifts/WeekPlan";
import {Shift} from "../pages/Shifts/Shift";
import {formatISO, parseISO} from "date-fns";
import {TokenAuthentication} from "../contexts/AuthenticationContext/Authentication.tsx"
import {Helper} from "../helpers/Helper.ts"
import {Booking, HelperBooking, NoBooking} from "../pages/Shifts/Booking.ts"

interface HelperBookingDto {
    type: string;
    id: string;
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
        let booking: Booking;
        if (shift.helperBooking.type === "Booking") {
            booking = HelperBooking.create({ id: shift.helperBooking.id });
        } else {
            booking = NoBooking.instance;
        }
        return Shift.create({
            shiftId: shift.shiftId,
            start: parseISO(shift.start),
            end: parseISO(shift.end),
            booking: booking,
        });
    }

    private toShiftDto(shift: Shift): ShiftDto {
        let helperBooking: HelperBookingDto
        if (shift.booking instanceof HelperBooking) {
            helperBooking = {
                type: "Booking",
                id: shift.booking.id,
            }
        } else {
            helperBooking = {
                type: "NoBooking",
                id: "N/A",
            }
        }

        return {
            shiftId: shift.shiftId,
            start: formatISO(shift.start),
            end: formatISO(shift.end),
            helperBooking,
        }
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

    async changeBooking(shiftId: string, helperId: string, token: TokenAuthentication): Promise<void> {
        console.log(`Changing shift ${shiftId} to have helper ${helperId}`)
        await this.client.put(`shifts/${shiftId}/booking`, helperId, {
            headers: {
                Authorization: `Bearer ${token.token}`,
            },
        });
    }

    async updateShift(shift: Shift, token: TokenAuthentication): Promise<void> {
        console.log(`Stub: Updating shift ${shift.shiftId}`, shift)
        const shiftDto = this.toShiftDto(shift)
        await this.client.put(`shifts/${shift.shiftId}`, shiftDto, {
            headers: {
                Authorization: `Bearer ${token.token}`,
            },
        });
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