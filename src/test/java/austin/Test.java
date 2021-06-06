package austin;

import java.io.IOException;

import org.restlet.data.MediaType;
import org.restlet.resource.ClientResource;

public class Test {

    public static void main(String[] args) throws IOException {

        // TODO 自動生成されたメソッド・スタブ
        final var clientResource = new ClientResource(
                "https://graph.facebook.com/v4.0/oauth/access_token?client_id=224818740984759&client_secret=3aff460359006f2a5f2ba26fe7ce5e78&code=AQB69Tc31uNiqMvF_EOSO7kNmmcRUR5_Bgh94UoA_HNdDp4D92pWslcPJs6yvr4FCPp4YO5s5tSPkwKil_yzZG8p1VXzuEItTzqgDfiWSJ4U5ypzW-AIn6RuKPbWmnl747B7CHOHK_CPYTEj_8W-eozKa2kNQ1eGhsSq5zhpestruLzVXpw7R_1sdIJg1gL2-XwLZSNU_erUy9rP9-qGTHsdzi_i9r-jSohAtyZxZbj6TyrwywvxO1UMlPCoC8kmAkMg488JAdO6AXTPCD5tkfK1futjW7LY7qn7UmtTWObiy8CzlUjxXK9Y_9K41ojCTC5FLTpw4mHFO3JA43Sv8inP");
        final var representation = clientResource.get(MediaType.APPLICATION_JSON);
        final var json = representation.getText();
        System.out.println(json);
    }

}
