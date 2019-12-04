package com.project.xero.util;

import com.project.xero.Model.Food;
import com.project.xero.Model.Rating;
import com.project.xero.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Rajat Sangrame on 3/12/19.
 * http://github.com/rajatsangrame
 */
public class Helper {


    // Map<Food,HashMap<User,Double>>
    private static Map<String, HashMap<String, Double>> mFoodData = new HashMap<>();
    // Map<Food,HashMap<Food,Double>>
    private static Map<String, HashMap<String, Double>> mSimilarityTable = new HashMap<>();

    public static List<String> getFoodRecommendation(User user, List<Rating> ratingList) {

        //region Fill mFoodData with available data
        for (Rating rat : ratingList) {

            if (!mFoodData.containsKey(rat.getFoodId())) {
                HashMap<String, Double> rating = new HashMap<>();
                rating.put(rat.getUserPhone(), rat.getRateValue());
                mFoodData.put(rat.getFoodId(), rating);

            } else {

                HashMap<String, Double> rating = mFoodData.get(rat.getFoodId());
                rating.put(rat.getUserPhone(), rat.getRateValue());
                mFoodData.put(rat.getFoodId(), rating);
            }
        }
        //endregion


        //Entry<Food, HashMap<User, Double>>
        for (Entry<String, HashMap<String, Double>> UserCol2 : mFoodData.entrySet()) {

            //Entry<Food, HashMap<User, Double>>
            for (Entry<String, HashMap<String, Double>> UserCol1 : mFoodData.entrySet()) {

                if (UserCol1.getKey().equals(UserCol2.getKey())) { // Food
                    // Todo: Add rating == 1 for this case
                    continue;
                }

                System.out.println(String.format("%s vs %s", UserCol2.getKey(), UserCol1.getKey()));

                //HashMap<User, Double>
                HashMap<String, Double> UserCol1Values = UserCol1.getValue();
                //HashMap<User, Double>
                HashMap<String, Double> UserCol2Values = UserCol2.getValue();

                List<Double> User2Rating = new ArrayList<>();
                List<Double> User1Rating = new ArrayList<>();

                //Entry<User, Double>
                for (Entry<String, Double> uUser2 : UserCol2Values.entrySet()) {

                    //Entry<User, Double>
                    for (Entry<String, Double> uUser1 : UserCol1Values.entrySet()) {

                        if (uUser2.getKey().equals(uUser1.getKey())) {

                            User2Rating.add(uUser2.getValue());
                            User1Rating.add(uUser1.getValue());
                            break;
                        }
                    }
                }

                // cos(v1,v2) = (5*2 + 3*3 + 1*3) / sqrt[(25+9+1) * (4+9+9)] = 0.792

                Double n = 0.0;
                Double d = 0.0;
                Double aaa = 0.0;
                Double bbb = 0.0;

                for (int i = 0; i < User1Rating.size(); i++) {
                    n = n + (User1Rating.get(i) * User2Rating.get(i));
                }
                for (int i = 0; i < User1Rating.size(); i++) {
                    aaa = aaa + Math.pow(User1Rating.get(i), 2.0);
                    bbb = bbb + Math.pow(User2Rating.get(i), 2.0);
                }
                d = Math.sqrt(aaa * bbb);
                Double theta = n / d;

                System.out.println(theta);

                if (!mSimilarityTable.containsKey(UserCol2.getKey())) {

                    //HashMap<Food, Double>
                    HashMap<String, Double> similarity = new HashMap<>();
                    similarity.put(UserCol1.getKey(), theta);
                    mSimilarityTable.put(UserCol2.getKey(), similarity);

                } else {

                    //HashMap<Food, Double>
                    HashMap<String, Double> similarity = mSimilarityTable.get(UserCol2.getKey());
                    similarity.put(UserCol1.getKey(), theta);
                    mSimilarityTable.put(UserCol2.getKey(), similarity);
                }
            }
        }

        System.out.println(String.format("Similarity Table Size %s", mSimilarityTable.size()));

        getRecommendationForFood(user.getName(), "");

        return null;
    }

    private static Double getRecommendationForFood(String userId, String bookId) {

        //Map<Food, Double>
        Map<String, Double> userData = new HashMap<>();

        //Entry<Food, HashMap<User, Double>>
        for (Entry<String, HashMap<String, Double>> data : mFoodData.entrySet()) {

            //HashMap<User, Double>
            HashMap<String, Double> mapData = data.getValue();

            if (mapData.containsKey(userId)) {
                userData.put(data.getKey(), mapData.get(userId));
            }
        }

        //Map<Food, Double>
        Map<String, Double> similarityForFoodData = new HashMap<>();

        //HashMap<Food, Double>
        HashMap<String, Double> UserData = mSimilarityTable.get("");

        //Entry<Food, Double>
        for (Entry<String, Double> itr : userData.entrySet()) {

            //(Entry<Food, Double>
            for (Entry<String, Double> itr2 : UserData.entrySet()) {

                if (itr2.getKey().equals(itr.getKey())) {

                    similarityForFoodData.put(itr.getKey(), itr2.getValue());
                }
            }
        }

        // (4*0.792 + 5*0.8) / (0.792+ 0.8) = 4.5
        Double n = 0.0;
        Double d = 0.0;

        //Entry<Food, Double>
        for (Entry<String, Double> itr : userData.entrySet()) {

            //Entry<Book, Double>
            for (Entry<String, Double> itr2 : similarityForFoodData.entrySet()) {

                if (itr2.getKey().equals(itr.getKey())) {

                    n = n + (itr.getValue() * itr2.getValue());
                    d = d + itr2.getValue();
                }
            }
        }

        Double result = n / d;
        System.out.println("ABCD");

        return result;
    }
    //endregion
}