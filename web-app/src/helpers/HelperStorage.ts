import {Helper} from "./Helper.ts"

const helpers = [
    Helper.create({
        id: "camilla",
        name: "Camilla Pedersen",
    }),
    Helper.create({
        id: "helle",
        name: "Helle Jakonsen",
    }),
    Helper.create({
        id: "stella",
        name: "Stella",
    }),
    Helper.create({
        id: "tex",
        name: "Tex Weerasinghe",
    }),
    Helper.create({
        id: "ulrik",
        name: "Ulrik Myrtue",
    }),
    Helper.create({
        id: "walter",
        name: "Walter Rodriques",
    }),
]

export class HelperStorage {
    all = helpers

    helper(id: string): Helper | undefined {
        return helpers.find(helper => helper.id === id)
    }
}