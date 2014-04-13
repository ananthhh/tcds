package views.test;

import static org.fest.assertions.Assertions.assertThat;
import static play.test.Helpers.contentAsString;
import static play.test.Helpers.contentType;

import java.util.List;

import org.junit.Test;

import com.smartsheet.api.Smartsheet;
import com.smartsheet.api.SmartsheetBuilder;
import com.smartsheet.api.SmartsheetException;
import com.smartsheet.api.models.Sheet;

import play.mvc.Content;

public class sendnewTest {
	
    @Test
    public void getAllSheets() throws SmartsheetException {
    	Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken("6fey44jw6g2l9rbguhmorttuob").build();
		List<Sheet> homeSheets = smartsheet.sheets().listSheets();
		
//		assertThat(homeSheets.size()).isEqualTo(1);
//		assertThat(homeSheets.get(0).getName()).isEqualTo("abdc");
		
		assertThat(homeSheets).isNotNull();

    }
    
    @Test
    public void getAllAttachments() throws SmartsheetException {
    	Smartsheet smartsheet = new SmartsheetBuilder().setAccessToken("6fey44jw6g2l9rbguhmorttuob").build();
    	List<Sheet> homeSheets = smartsheet.sheets().listSheets();
    	Sheet sheet = homeSheets.get(0);
    	assertThat(sheet.getName()).isEqualTo("abc");
    	System.out.println(sheet.getColumns().size());
//    	assertThat(sheet.getAttachments().size()).isEqualTo(1);

    }
    
}
