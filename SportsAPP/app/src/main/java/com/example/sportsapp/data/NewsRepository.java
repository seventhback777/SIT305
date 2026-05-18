package com.example.sportsapp.data;

import com.example.sportsapp.R;
import com.example.sportsapp.model.NewsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Single source of truth for all hardcoded news data.
 * All Fragments and ViewModels fetch data from here.
 */
public class NewsRepository {

    private static NewsRepository instance;
    private final List<NewsItem> allNews = new ArrayList<>();

    private NewsRepository() {
        loadData();
    }

    public static NewsRepository getInstance() {
        if (instance == null) {
            instance = new NewsRepository();
        }
        return instance;
    }

    private void loadData() {
        // --- Featured items (isFeatured = true) ---
        allNews.add(new NewsItem(
                1,
                "Premier League: Man City vs Arsenal",
                "The title race heats up as Manchester City host Arsenal at the Etihad Stadium. "
                + "Both sides are level on points with just six games remaining. "
                + "City have won their last five home games, while Arsenal are unbeaten in eleven. "
                + "This clash could define who lifts the trophy in May. "
                + "Pep Guardiola has confirmed all key players are fit and available.",
                "Apr 10, 2026",
                R.drawable.bg_football,
                "Football",
                true
        ));
        allNews.add(new NewsItem(
                2,
                "NBA Finals: Lakers vs Celtics – Game 5",
                "The rivalry resumes as the Los Angeles Lakers face the Boston Celtics in a winner-takes-all Game 5. "
                + "LeBron James is chasing his fifth championship ring, while the Celtics look to avenge last year's defeat. "
                + "Tip-off is set for 8 PM ET at TD Garden. "
                + "The series has been defined by big fourth-quarter swings, and Game 5 promises more drama. "
                + "Both coaches have kept their rotations tight, and fan interest is at an all-time high.",
                "Apr 11, 2026",
                R.drawable.bg_basketball,
                "Basketball",
                true
        ));
        allNews.add(new NewsItem(
                3,
                "ICC World Cup Final: Australia vs India",
                "Cricket's biggest stage is set as Australia take on India in the ICC World Cup Final. "
                + "India are the defending champions and slight favourites coming into the match. "
                + "Australia's pace attack has been in devastating form throughout the tournament. "
                + "The match will be played at the Melbourne Cricket Ground in front of 100,000 fans. "
                + "Both captains confirmed their strongest XI for this historic encounter.",
                "Apr 9, 2026",
                R.drawable.bg_cricket,
                "Cricket",
                true
        ));

        // --- Football news ---
        allNews.add(new NewsItem(
                4,
                "Premier League Title Race: Who Has the Edge?",
                "With six gameweeks left, pundits and fans are debating which club has the mental edge. "
                + "Manchester City's experience in title battles is unmatched, having won four of the last five. "
                + "Arsenal's young squad brings hunger and press intensity that City have struggled to contain. "
                + "Goal difference currently separates the two sides, making every result crucial. "
                + "Former champions weigh in on who they believe will hold their nerve.",
                "Apr 11, 2026",
                R.drawable.bg_football,
                "Football",
                false
        ));
        allNews.add(new NewsItem(
                5,
                "Injury Update: Star Midfielder Out for Six Weeks",
                "Liverpool's midfield options have been hit hard as their creative midfielder suffered a hamstring tear "
                + "in training on Thursday. The club confirmed he will miss six weeks of action. "
                + "This rules him out of two crucial Champions League legs and four Premier League fixtures. "
                + "Manager Slot has confirmed he will rotate his remaining options to cover the absence. "
                + "The injury is a major blow to Liverpool's European ambitions.",
                "Apr 10, 2026",
                R.drawable.bg_football,
                "Football",
                false
        ));
        allNews.add(new NewsItem(
                6,
                "Champions League Quarter-Finals: Preview",
                "Europe's elite clubs clash this week as the Champions League quarter-finals get underway. "
                + "Real Madrid face Bayern Munich in a rematch of last year's semi-final thriller. "
                + "PSG host Barcelona in a mouth-watering all-Ligue 1 vs La Liga encounter. "
                + "Both ties are perfectly balanced and impossible to call. "
                + "Tactical analysis suggests the first legs could be cagey affairs decided by set pieces.",
                "Apr 8, 2026",
                R.drawable.bg_football,
                "Football",
                false
        ));

        // --- Basketball news ---
        allNews.add(new NewsItem(
                7,
                "Curry Breaks Three-Point Record – Again",
                "Stephen Curry has shattered yet another three-point milestone this week, setting a new NBA record "
                + "for most threes in a single regular season. The Golden State guard drained 12 three-pointers "
                + "across two games to claim the record he previously held himself. "
                + "Head coach Steve Kerr called it 'the greatest shooting performance I've ever witnessed.' "
                + "Curry is averaging an astonishing 6.4 threes per game this season.",
                "Apr 9, 2026",
                R.drawable.bg_basketball,
                "Basketball",
                false
        ));
        allNews.add(new NewsItem(
                8,
                "2026 NBA Draft: Top Five Prospects Ranked",
                "With the regular season winding down, scouts and general managers are finalising their draft boards. "
                + "The consensus top pick is a 19-year-old point guard from France who averaged 24 points in the EuroLeague. "
                + "Two college players from the SEC conference are generating serious lottery buzz. "
                + "This draft class is considered the deepest since 2003, with elite depth through the top 15 picks. "
                + "Teams holding lottery picks are already preparing trade packages.",
                "Apr 7, 2026",
                R.drawable.bg_basketball,
                "Basketball",
                false
        ));
        allNews.add(new NewsItem(
                9,
                "Trade Deadline Recap: The Biggest Moves",
                "The NBA trade deadline produced a flurry of blockbuster deals in its final hours. "
                + "A former MVP changed teams for the first time in eight years in a stunning three-team trade. "
                + "Two contenders bolstered their benches with veterans ahead of the playoff push. "
                + "Several rebuilding teams shed salary and collected future first-round picks. "
                + "Analysts are already debating which teams improved most on paper.",
                "Apr 6, 2026",
                R.drawable.bg_basketball,
                "Basketball",
                false
        ));

        // --- Cricket news ---
        allNews.add(new NewsItem(
                10,
                "IPL 2026 Auction: Record-Breaking Bids",
                "The IPL 2026 mega auction saw franchises spend a record combined total of ₹1,200 crore over two days. "
                + "A 22-year-old fast bowler became the most expensive player in IPL history at ₹27 crore. "
                + "Several international stars attracted fierce bidding wars, with four players passing the ₹20 crore mark. "
                + "Mumbai Indians and Chennai Super Kings were the most active bidders on day one. "
                + "Full squad lists will be released before the season opener in March.",
                "Apr 8, 2026",
                R.drawable.bg_cricket,
                "Cricket",
                false
        ));
        allNews.add(new NewsItem(
                11,
                "England Tour of New Zealand: Test Series Begins",
                "England begin their three-match Test series in New Zealand this weekend with a full-strength squad. "
                + "The Kiwis are the top-ranked Test side in the world and formidable at home. "
                + "England's new head coach has promised aggressive, positive cricket regardless of conditions. "
                + "Pitch reports suggest the first Test in Wellington will heavily favour seamers. "
                + "Both captains completed their pre-match press conferences on Friday.",
                "Apr 10, 2026",
                R.drawable.bg_cricket,
                "Cricket",
                false
        ));
        allNews.add(new NewsItem(
                12,
                "Women's Cricket: Australia Clinch ODI Series",
                "The Australian women's side sealed a dominant 4-1 ODI series victory over South Africa in Sydney. "
                + "Captain Alyssa Healy led from the front with back-to-back half-centuries in the final two matches. "
                + "The win extends Australia's record home ODI winning streak to 18 matches. "
                + "South Africa's coach praised the learning experience despite the result. "
                + "Australia now turn their attention to next month's ICC Women's Championship fixtures.",
                "Apr 7, 2026",
                R.drawable.bg_cricket,
                "Cricket",
                false
        ));

        // --- Tennis news ---
        allNews.add(new NewsItem(
                13,
                "Wimbledon 2026 Draw Released",
                "The Wimbledon 2026 draw has been released, setting up potential blockbuster quarter-final clashes. "
                + "The top two seeds are placed in opposite halves of the draw, ensuring a final showdown if both progress. "
                + "Three wildcard entries have been handed to rising British talents. "
                + "Last year's runner-up has been seeded fifth and faces a tough first-week path. "
                + "Play begins on June 29, with the men's and women's finals scheduled for the second Sunday.",
                "Apr 11, 2026",
                R.drawable.bg_tennis,
                "Tennis",
                false
        ));
        allNews.add(new NewsItem(
                14,
                "World No.1 Dominates Clay Season Opener",
                "The world number one continued their imperious clay-court form with a straight-sets victory "
                + "in the Monte-Carlo Masters final, dropping just 14 games across the entire tournament. "
                + "The performance has raised expectations ahead of the French Open in May. "
                + "Opponents have struggled to find an answer to their consistency from the baseline. "
                + "Coaches on the tour have called the display 'as close to perfect clay-court tennis as you'll see.'",
                "Apr 9, 2026",
                R.drawable.bg_tennis,
                "Tennis",
                false
        ));
        allNews.add(new NewsItem(
                15,
                "US Open 2025 Review: A Season-Defining Final",
                "Looking back at last year's US Open final, which is already being called one of the greatest "
                + "matches in Flushing Meadows history. The five-set encounter lasted four hours and forty minutes. "
                + "Both players produced tennis of the highest quality under enormous pressure. "
                + "The champion described the win as 'the most important moment of my career.' "
                + "Ticket demand for the 2026 edition has broken pre-sale records following last year's classic.",
                "Apr 5, 2026",
                R.drawable.bg_tennis,
                "Tennis",
                false
        ));
    }

    /** Returns all news items */
    public List<NewsItem> getAllNews() {
        return allNews;
    }

    /** Returns only featured items */
    public List<NewsItem> getFeaturedNews() {
        List<NewsItem> featured = new ArrayList<>();
        for (NewsItem item : allNews) {
            if (item.isFeatured()) featured.add(item);
        }
        return featured;
    }

    /** Returns non-featured items, optionally filtered by category ("All" = no filter) */
    public List<NewsItem> getNewsByCategory(String category) {
        List<NewsItem> result = new ArrayList<>();
        for (NewsItem item : allNews) {
            if (item.isFeatured()) continue;
            if (category.equals("All") || item.getCategory().equals(category)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Find a single item by id */
    public NewsItem getNewsById(int id) {
        for (NewsItem item : allNews) {
            if (item.getId() == id) return item;
        }
        return null;
    }

    /** Related news: same category, not the current item, not featured */
    public List<NewsItem> getRelatedNews(int currentId, String category) {
        List<NewsItem> related = new ArrayList<>();
        for (NewsItem item : allNews) {
            if (item.getId() == currentId || item.isFeatured()) continue;
            if (item.getCategory().equals(category)) related.add(item);
        }
        return related;
    }

    /** Fetch full NewsItem list for a set of bookmarked ids */
    public List<NewsItem> getNewsForIds(List<Integer> ids) {
        List<NewsItem> result = new ArrayList<>();
        for (NewsItem item : allNews) {
            if (ids.contains(item.getId())) result.add(item);
        }
        return result;
    }
}
