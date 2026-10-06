package com.example.nutrimind;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
public interface ApiService {
    //    @POST("/predict-mood")
//    Call<MoodResponse> predictMood(@Body MoodRequest request);
    // Predicting moods based on the text
    @POST("/predict")
    Call<EmotionResponse> predictEmotion(@Body MoodRequest request);
}