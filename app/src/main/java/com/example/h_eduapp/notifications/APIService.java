package com.example.h_eduapp.notifications;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {



    @Headers({

            "Content-type:application/json",
            "Authorization:key=AAAA_3xU70w:APA91bFHhr6DmJ9BTQFFucefE6Qg_1ILBpt_IvtAr670YWoLV-MR3bOYgtr6VxL3fRQahHQsEkTjDzdAq98YT42gbZ1V5i4kgORjKmjn6-ujpNLufmNzbPhob3npHx_VajUfw5lkLk_-"
    })

    @POST("fcm/send")
    Call<Respone> sendNotificatioon(@Body Sender body);

}
