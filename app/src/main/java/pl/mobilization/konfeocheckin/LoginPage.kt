package pl.mobilization.konfeocheckin

import pl.droidsonroids.jspoon.annotation.Selector

/**
 * Created by marekdef on 27.12.17.
 */


class LoginPage {
    @Selector("body > div.content > form > input[name='authenticity_token']", attr = "value", index = 0)
    var  authenticity_token : String = ""
    @Selector("head > title")
    var  title : String = ""
}