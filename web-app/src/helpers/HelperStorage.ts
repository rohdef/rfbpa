import {Helper} from "./Helper.ts"
import {brown, lightGreen, pink, purple, yellow} from "@mui/material/colors"

const helpers = [
    Helper.create({
        id: "camilla",
        name: "Camilla Pedersen",
        color: lightGreen["A100"],
        filtered: true
    }),
    Helper.create({
        id: "helle",
        name: "Helle Jakonsen",
        color: purple["100"],
        filtered: false
    }),
    Helper.create({
        id: "stella",
        name: "Stella",
        color: pink["200"],
        filtered: false
    }),
    Helper.create({
        id: "tex",
        name: "Tex Weerasinghe",
        color: brown["100"],
        filtered: false
    }),
    Helper.create({
        id: "ulrik",
        name: "Ulrik Myrtue",
        color: pink["100"],
        filtered: false
    }),
    Helper.create({
        id: "walter",
        name: "Walter Rodriques",
        color: yellow["100"],
        filtered: false
    }),
]

export class HelperStorage {
    all = helpers

    helper(id: string): Helper | undefined {
        return helpers.find(helper => helper.id === id)
    }
}