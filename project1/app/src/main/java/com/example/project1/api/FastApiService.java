package com.example.project1.api;

import com.example.project1.model.Card;
import com.example.project1.model.LoginResponse;
import com.example.project1.model.NewPassword;
import com.example.project1.model.ResetPasswordResponse;
import com.example.project1.model.Task;
import com.example.project1.model.Updateuser;
import com.example.project1.model.User;
import com.example.project1.model.UserDTO;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FastApiService {
    @POST("/signup")
    Call<JsonObject> registerUser(@Body User user);
    @POST("signin") // Endpoint path relative to the base URL
    Call<LoginResponse> loginUser(@Body UserDTO user);

    @POST("add_task") // Endpoint path relative to the base URL
    Call<Response<ResponseBody>> addtask(@Body Task task);

    @PUT("/update_task/{task_id}")
    Call<Response<ResponseBody>> updateTask(
            @Path("task_id") int taskId,
            @Query("owner_id") int ownerId,
            @Body Task updatedData
    );


    @GET("get_running_tasks")
    Call<List<Card>> getCardData();

    @GET("/getTaskById/{task_id}")
    Call<Card> getTaskByid(@Path("task_id") int task_id);


    @PUT("/complete_task/{task_id}")
    Call<Response<ResponseBody>> completeTask(
            @Path("task_id") int task_Id,
            @Query("owner_id") int owner_Id
    );
    @POST("/password")
    Call<String> resetPassword(@Query("phone") String userPhone);

//    @PUT("/reset_password")
//    Call<NewPassword> resetPassword(@Body NewPassword newPassword);

    @PUT("/reset_password")
    Call<ResetPasswordResponse> reset_password(@Body ResetPasswordResponse reset_data);
//    @POST("add_task") // Endpoint path relative to the base URL
//    Call<Response<ResponseBody>> registerUser(@Body User user);

    @PUT("/update_profile")
    Call<Void> updateProfile(@Query("phone") String phone, @Body Updateuser updatedData);

    @GET("/get_user/{user_id}")
    Call<User> getUser(@Path("user_id") int userId);

//    @PUT("/update_profile/{phone}")
//    Call<Map<String, String>> updateProfile(@Path("phone") String phone, @Body User updatedData);
}

