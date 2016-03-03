import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import controllers.SqlConn;
import org.junit.*;

import play.mvc.*;
import play.test.*;
import play.data.DynamicForm;
import play.data.validation.ValidationError;
import play.data.validation.Constraints.RequiredValidator;
import play.i18n.Lang;
import play.libs.F;
import play.libs.F.*;
import play.twirl.api.Content;

import static play.test.Helpers.*;
import static org.junit.Assert.*;


/**
*
* Simple (JUnit) tests that can call all parts of a play app.
* If you are interested in mocking a whole application, see the wiki for more details.
*
*/
public class ApplicationTest {

    @Test
    public void simpleCheck() {
        int a = 1 + 1;
        assertEquals(2, a);
    }

    @Test
    public void getSchema() {
        SqlConn s = new SqlConn("will");
        List<String> tbList = s.readTableList();
        assertNotNull(tbList);
        for(String str: tbList) {
            System.out.println(str);
        }
    }

    @Test
    public void getQueryAns() {
        SqlConn s = new SqlConn("will");
        List<List<String>> res = s.query("count(pubkey)", "article", "year < 1970", "year", null, null);
        for (List<String> list: res) {
            for (String str: list) {
                System.out.println(str);
            }
        }
    }


}
