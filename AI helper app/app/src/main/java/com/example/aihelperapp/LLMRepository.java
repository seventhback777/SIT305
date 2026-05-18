package com.example.aihelperapp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LLMRepository {

    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + BuildConfig.GEMINI_API_KEY;

    private final OkHttpClient client = new OkHttpClient();

    public interface LLMCallback {
        void onSuccess(String result);
        void onError(String error);
    }

    public interface TasksCallback {
        void onSuccess(List<Task> tasks);
        void onError(String error);
    }

    public interface QuizCallback {
        void onSuccess(List<QuizQuestion> questions);
        void onError(String error);
    }

    public interface ExplanationCallback {
        void onSuccess(List<String> explanations);
        void onError(String error);
    }

    // ── 1. Generate tasks based on user interests ──────────────────────────
    public void generateTasks(List<String> topics, int userId, TasksCallback callback) {
        String topicList = String.join(", ", topics);
        String prompt = "You are a learning assistant. Generate exactly 5 personalized learning tasks "
                + "for a student interested in: " + topicList + ". "
                + "Respond ONLY with a JSON array, no markdown, no explanation. Format:\n"
                + "[{\"title\":\"Task title\",\"description\":\"1-2 sentence description\",\"topic\":\"relevant topic\"}]";

        callGemini(prompt, new LLMCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    String cleaned = cleanJson(result);
                    JsonArray array = JsonParser.parseString(cleaned).getAsJsonArray();
                    List<Task> tasks = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject obj = array.get(i).getAsJsonObject();
                        Task task = new Task();
                        task.title = obj.get("title").getAsString();
                        task.description = obj.get("description").getAsString();
                        task.topic = obj.get("topic").getAsString();
                        task.userId = userId;
                        task.isCompleted = false;
                        tasks.add(task);
                    }
                    callback.onSuccess(tasks);
                } catch (Exception e) {
                    callback.onError("Failed to parse tasks: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ── 2. Generate quiz questions for a task ──────────────────────────────
    public void generateQuiz(String taskTitle, String taskDescription, QuizCallback callback) {
        String prompt = "You are a quiz generator. Create exactly 5 multiple-choice questions "
                + "to test knowledge about this learning task:\n"
                + "Title: " + taskTitle + "\n"
                + "Description: " + taskDescription + "\n"
                + "Respond ONLY with a JSON array, no markdown, no explanation. Format:\n"
                + "[{\"question\":\"Question text?\","
                + "\"optionA\":\"First option\","
                + "\"optionB\":\"Second option\","
                + "\"optionC\":\"Third option\","
                + "\"optionD\":\"Fourth option\","
                + "\"correctAnswer\":\"A\"}]"
                + "\ncorrectAnswer must be exactly one of: A, B, C, D";

        callGemini(prompt, new LLMCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    String cleaned = cleanJson(result);
                    JsonArray array = JsonParser.parseString(cleaned).getAsJsonArray();
                    List<QuizQuestion> questions = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject obj = array.get(i).getAsJsonObject();
                        QuizQuestion q = new QuizQuestion();
                        q.question = obj.get("question").getAsString();
                        q.optionA = obj.get("optionA").getAsString();
                        q.optionB = obj.get("optionB").getAsString();
                        q.optionC = obj.get("optionC").getAsString();
                        q.optionD = obj.get("optionD").getAsString();
                        q.correctAnswer = obj.get("correctAnswer").getAsString();
                        questions.add(q);
                    }
                    callback.onSuccess(questions);
                } catch (Exception e) {
                    callback.onError("Failed to parse quiz: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ── 3. Explain answers for result page ────────────────────────────────
    public void explainAnswers(List<QuizHistory> histories, ExplanationCallback callback) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("For each of the following quiz questions, provide a brief 1-2 sentence explanation "
                + "of why the correct answer is correct. "
                + "Respond ONLY with a JSON array of strings (one explanation per question), no markdown.\n");
        for (int i = 0; i < histories.size(); i++) {
            QuizHistory h = histories.get(i);
            prompt.append((i + 1)).append(". Question: ").append(h.questionText)
                    .append(" | Correct answer: ").append(h.correctAnswer)
                    .append(" | User selected: ").append(h.selectedAnswer).append("\n");
        }
        prompt.append("\nRespond with exactly ").append(histories.size())
                .append(" explanation strings in a JSON array like: [\"explanation1\",\"explanation2\"]");

        callGemini(prompt.toString(), new LLMCallback() {
            @Override
            public void onSuccess(String result) {
                try {
                    String cleaned = cleanJson(result);
                    JsonArray array = JsonParser.parseString(cleaned).getAsJsonArray();
                    List<String> explanations = new ArrayList<>();
                    for (int i = 0; i < array.size(); i++) {
                        explanations.add(array.get(i).getAsString());
                    }
                    callback.onSuccess(explanations);
                } catch (Exception e) {
                    callback.onError("Failed to parse explanations: " + e.getMessage());
                }
            }

            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }

    // ── 4. Summarise wrong answers for profile page ───────────────────────
    public void summarizeWrongAnswers(List<QuizHistory> wrongAnswers, LLMCallback callback) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a direct and honest learning coach. The student got ")
                .append(wrongAnswers.size()).append(" question(s) wrong. ")
                .append("Here are the incorrect answers:\n\n");
        for (int i = 0; i < wrongAnswers.size(); i++) {
            QuizHistory h = wrongAnswers.get(i);
            prompt.append((i + 1)).append(". ").append(h.questionText).append("\n");
            prompt.append("   Their answer: ").append(h.selectedAnswer)
                    .append(" | Correct answer: ").append(h.correctAnswer).append("\n");
        }
        prompt.append("\nIn 3-4 sentences: (1) honestly assess the student's understanding based on these mistakes, ");
        prompt.append("(2) identify the specific topics they need to study, ");
        prompt.append("(3) give concrete actionable steps to improve. ");
        prompt.append("Do NOT say 'great effort' or give unwarranted praise. Be honest, specific, and constructive.");
        callGemini(prompt.toString(), callback);
    }

    // ── Core HTTP method ───────────────────────────────────────────────────
    private void callGemini(String prompt, LLMCallback callback) {
        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\""
                + escapeJson(prompt) + "\"}]}]}";

        android.util.Log.d("LLM", "Calling URL: " + API_URL);
        android.util.Log.d("LLM", "Request body (first 200): " + requestBody.substring(0, Math.min(200, requestBody.length())));

        Request request = new Request.Builder()
                .url(API_URL)
                .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                android.util.Log.e("LLM", "Network failure: " + e.getMessage());
                callback.onError(e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body().string();
                android.util.Log.d("LLM", "HTTP status: " + response.code());
                android.util.Log.d("LLM", "Response body: " + body);
                if (!response.isSuccessful()) {
                    callback.onError("API error " + response.code() + ": " + body);
                    return;
                }
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    String text = json.getAsJsonArray("candidates")
                            .get(0).getAsJsonObject()
                            .getAsJsonObject("content")
                            .getAsJsonArray("parts")
                            .get(0).getAsJsonObject()
                            .get("text").getAsString();
                    callback.onSuccess(text);
                } catch (Exception e) {
                    android.util.Log.e("LLM", "Parse error: " + e.getMessage());
                    callback.onError("Failed to parse API response: " + e.getMessage());
                }
            }
        });
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String cleanJson(String raw) {
        String s = raw.trim();
        if (s.startsWith("```")) {
            int start = s.indexOf('\n');
            int end = s.lastIndexOf("```");
            if (start != -1 && end > start) {
                s = s.substring(start + 1, end).trim();
            }
        }
        return s;
    }

    public static class QuizQuestion {
        public String question;
        public String optionA;
        public String optionB;
        public String optionC;
        public String optionD;
        public String correctAnswer;
    }
}
