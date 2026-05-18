package com.example.videoapp.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.videoapp.data.AppDatabase;
import com.example.videoapp.data.UserDao;
import com.example.videoapp.model.User;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserViewModel extends AndroidViewModel {

    public static final int RESULT_SUCCESS = 0;
    public static final int RESULT_INVALID_CREDENTIALS = 1;
    public static final int RESULT_USERNAME_TAKEN = 2;
    public static final int RESULT_ERROR = 3;

    private final UserDao userDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public static final int RESULT_LOGIN_SUCCESS = 10;
    public static final int RESULT_LOGIN_USER_NOT_FOUND = 11;
    public static final int RESULT_LOGIN_WRONG_PASSWORD = 12;

    // Emits the logged-in User on success, null on failure
    public final MutableLiveData<User> loginResult = new MutableLiveData<>();
    public final MutableLiveData<Integer> loginError = new MutableLiveData<>();
    // Emits a result code for registration
    public final MutableLiveData<Integer> registerResult = new MutableLiveData<>();

    public UserViewModel(@NonNull Application application) {
        super(application);
        userDao = AppDatabase.getInstance(application).userDao();
    }

    public void login(String username, String password) {
        executor.execute(() -> {
            User user = userDao.findByUsername(username.trim());
            if (user == null) {
                loginError.postValue(RESULT_LOGIN_USER_NOT_FOUND);
                loginResult.postValue(null);
            } else if (!user.password.equals(password)) {
                loginError.postValue(RESULT_LOGIN_WRONG_PASSWORD);
                loginResult.postValue(null);
            } else {
                loginError.postValue(RESULT_LOGIN_SUCCESS);
                loginResult.postValue(user);
            }
        });
    }

    public void register(String fullName, String username, String password) {
        executor.execute(() -> {
            // Check username not taken
            User existing = userDao.findByUsername(username.trim());
            if (existing != null) {
                registerResult.postValue(RESULT_USERNAME_TAKEN);
                return;
            }
            try {
                User newUser = new User(fullName.trim(), username.trim(), password);
                userDao.insert(newUser);
                registerResult.postValue(RESULT_SUCCESS);
            } catch (Exception e) {
                registerResult.postValue(RESULT_ERROR);
            }
        });
    }
}
