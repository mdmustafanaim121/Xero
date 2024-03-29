package com.project.xero.Service;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.project.xero.Common.Common;
import com.project.xero.Model.Token;

public class FirebaseInstanceService extends FirebaseInstanceIdService {



    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();
        String tokenrefresh = FirebaseInstanceId.getInstance().getToken();
        if(Common.curUser != null)
            updateToken(tokenrefresh);
    }

    private void updateToken(String tokenrefresh) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token token = new Token(tokenrefresh,false); //false because this token is from Client App
        tokens.child(Common.curUser.getPhone()).setValue(token);
    }
}
