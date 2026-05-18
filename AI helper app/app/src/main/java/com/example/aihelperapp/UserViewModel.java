package com.example.aihelperapp;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import androidx.lifecycle.LiveData;

import java.util.List;

public class UserViewModel extends AndroidViewModel {
    private static final String PREFS_NAME = "learning_prefs";
    private static final String KEY_USER_ID = "user_id";

    private final AppDatabase db;
    private final SharedPreferences prefs;

    public final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    public final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    public final MutableLiveData<Boolean> interestSaved = new MutableLiveData<>();

    public UserViewModel(Application application) {
        super(application);
        db = AppDatabase.getDatabase(application);
        prefs = application.getSharedPreferences(PREFS_NAME, 0);
    }

    public int getLoggedInUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public boolean isLoggedIn() {
        return getLoggedInUserId() != -1;
    }

    public void logout() {
        prefs.edit().remove(KEY_USER_ID).apply();
        // 重置所有 LiveData，防止回到 LoginFragment 时粘性事件触发
        loginSuccess.postValue(null);
        registerSuccess.postValue(null);
        interestSaved.postValue(null);
        errorMessage.postValue(null);
    }

    public void login(String username, String password) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            User user = db.userDao().login(username, password);
            if (user != null) {
                prefs.edit().putInt(KEY_USER_ID, user.id).apply();
                loginSuccess.postValue(true);
            } else {
                errorMessage.postValue("Invalid username or password");
                loginSuccess.postValue(false);
            }
        });
    }

    public void register(String fullName, String username, String email,
                         String confirmEmail, String password, String confirmPassword, String phone) {
        if (!email.equals(confirmEmail)) {
            errorMessage.postValue("Emails do not match");
            return;
        }
        if (!password.equals(confirmPassword)) {
            errorMessage.postValue("Passwords do not match");
            return;
        }
        AppDatabase.databaseWriteExecutor.execute(() -> {
            if (db.userDao().findByUsername(username) != null) {
                errorMessage.postValue("Username already taken");
                return;
            }
            if (db.userDao().findByEmail(email) != null) {
                errorMessage.postValue("Email already registered");
                return;
            }
            User user = new User();
            user.fullName = fullName;
            user.username = username;
            user.email = email;
            user.phoneNumber = phone;
            user.password = password;
            long newId = db.userDao().insert(user);
            prefs.edit().putInt(KEY_USER_ID, (int) newId).apply();
            registerSuccess.postValue(true);
        });
    }

    public void saveInterests(List<Integer> topicIds) {
        int userId = getLoggedInUserId();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.topicDao().clearUserTopics(userId);
            for (int topicId : topicIds) {
                UserTopic ut = new UserTopic();
                ut.userId = userId;
                ut.topicId = topicId;
                db.topicDao().insertUserTopic(ut);
            }
            interestSaved.postValue(true);
        });
    }

    public MutableLiveData<List<Topic>> getAllTopics() {
        MutableLiveData<List<Topic>> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() ->
                liveData.postValue(db.topicDao().getAllTopics()));
        return liveData;
    }

    public MutableLiveData<Boolean> hasInterests() {
        MutableLiveData<Boolean> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() ->
                liveData.postValue(db.topicDao().getUserTopicCount(getLoggedInUserId()) > 0));
        return liveData;
    }

    public MutableLiveData<User> getUserLive(int userId) {
        MutableLiveData<User> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() ->
                liveData.postValue(db.userDao().findById(userId)));
        return liveData;
    }

    /** Returns int[]{total, correct, incorrect} */
    public MutableLiveData<int[]> getQuizStats(int userId) {
        MutableLiveData<int[]> liveData = new MutableLiveData<>();
        AppDatabase.databaseWriteExecutor.execute(() -> {
            int total = db.quizHistoryDao().getTotalCount(userId);
            int correct = db.quizHistoryDao().getCorrectCount(userId);
            liveData.postValue(new int[]{total, correct, total - correct});
        });
        return liveData;
    }

    public LiveData<List<QuizHistory>> getHistoryLive(int userId) {
        return db.quizHistoryDao().getHistoryLive(userId);
    }

    public void summarizeWrongAnswers(int userId, LLMRepository.LLMCallback callback) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            List<QuizHistory> wrong = db.quizHistoryDao().getWrongAnswers(userId);
            if (wrong.isEmpty()) {
                callback.onSuccess("Great job! You have no incorrect answers yet. Keep practising to maintain your streak!");
                return;
            }
            new LLMRepository().summarizeWrongAnswers(wrong, callback);
        });
    }
}
