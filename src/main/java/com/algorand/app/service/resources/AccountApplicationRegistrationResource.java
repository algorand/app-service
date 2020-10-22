package com.algorand.app.service.resources;

import com.algorand.app.service.core.AccountApplicationRegistration;
import com.algorand.app.service.core.UserAccountRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

@Path("/account-application-registration")
public class AccountApplicationRegistrationResource {
    private Logger log = LoggerFactory.getLogger("AccountApplicationRegistrationResource");

    private Map<String, AccountApplicationRegistration> accountApplicationRegistrationMap = new HashMap();
    private Map<String, UserAccountRegistration> userAccountRegistrationMap = new HashMap<>();

    @GET
    @Path("/user-account")
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserAccountRegistration> getUserAccountRegistrationList() {
        log.info("getUserAccountRegistrationList " + userAccountRegistrationMap);
        return  new ArrayList<UserAccountRegistration>(userAccountRegistrationMap.values());
    }

    @POST
    @Path("/user-account")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createUserAccountRegistrationList(UserAccountRegistration userAccountRegistration) {
        log.info("createUserAccountRegistration " + userAccountRegistration);
        userAccountRegistrationMap.put(userAccountRegistration.getAlias(),userAccountRegistration );
    }


    /**
     * Return an individual AccountApplicationRegistration
     * @param accountApplicationRegistration
     * @return
     */
    @GET
    @Path("{accountApplicationRegistration}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountApplicationRegistration getTransactionEnvelope(@PathParam("accountApplicationRegistration") String accountApplicationRegistration) {
        return accountApplicationRegistrationMap.get(accountApplicationRegistration );
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<AccountApplicationRegistration> getAccountApplicationRegistrationList() {
        return  new ArrayList<AccountApplicationRegistration>(accountApplicationRegistrationMap.values());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public void createAccountApplicationRegistration(AccountApplicationRegistration accountApplicationRegistration) {
        accountApplicationRegistrationMap.put(accountApplicationRegistration.getId(),accountApplicationRegistration );
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateAccountApplicationRegistration(AccountApplicationRegistration accountApplicationRegistration) {
        accountApplicationRegistrationMap.put(accountApplicationRegistration.getId(),accountApplicationRegistration );
    }

    @DELETE
    public void deleteAccountApplicationRegistration(String accountApplicationRegistrationId) {
        accountApplicationRegistrationMap.remove(accountApplicationRegistrationId );
    }

    @GET
    @Path("generate/{account}/{application}/{walletId}")
    @Produces(MediaType.APPLICATION_JSON)
    public AccountApplicationRegistration generateAccountApplicationRegistration(
            @PathParam("account") String account,
            @PathParam("application") String application,
            @PathParam("walletId") String walletId ) {

        AccountApplicationRegistration aar = new AccountApplicationRegistration();
        aar.setAccountAddress(account);
        aar.setApplicationId(application);
        aar.setWalletIdentifier(walletId);

        accountApplicationRegistrationMap.put(aar.getId(), aar);

        return aar;
    }

}
