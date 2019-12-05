package com.project.xero.util;

import com.project.xero.Model.Rating;
import com.project.xero.Model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


public class Helper {


    // Map<Food,HashMap<User,Double>>
    private static Map<String, HashMap<String, Double>> mFoodData;
    // Map<Food,HashMap<Food,Double>>
    private static Map<String, HashMap<String, Double>> mSimilarityTable;

    public static HashMap<String,Double> getFoodRecommendation(User user, List<Rating> ratingList,
                                                     List<String> foodList) {

        mFoodData = new HashMap<>();
        mSimilarityTable = new HashMap<>();

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
                    continue;
                }

                System.out.println(String.format("%s vs %s", UserCol2.getKey(), UserCol1.getKey()));

                //HashMap<User, Double>
                HashMap<String, Double> UserCol1Values = UserCol1.getValue();
                //HashMap<User, Double>
                HashMap<String, Double> UserCol2Values = UserCol2.getValue();

                List<Double> food2Rating = new ArrayList<>();
                List<Double> food1Rating = new ArrayList<>();

                //Entry<User, Double>
                for (Entry<String, Double> uFood2 : UserCol2Values.entrySet()) {

                    //Entry<User, Double>
                    for (Entry<String, Double> uFood1 : UserCol1Values.entrySet()) {

                        if (uFood2.getKey().equals(uFood1.getKey())) {

                            food2Rating.add(uFood2.getValue());
                            food1Rating.add(uFood1.getValue());
                            break;
                        }
                    }
                }

                // cos(v1,v2) = (5*2 + 3*3 + 1*3) / sqrt[(25+9+1) * (4+9+9)] = 0.792

                Double n = 0.0;
                Double d = 0.0;
                Double aaa = 0.0;
                Double bbb = 0.0;

                for (int i = 0; i < food1Rating.size(); i++) {
                    n = n + (food1Rating.get(i) * food2Rating.get(i));
                }
                for (int i = 0; i < food1Rating.size(); i++) {
                    aaa = aaa + Math.pow(food1Rating.get(i), 2.0);
                    bbb = bbb + Math.pow(food2Rating.get(i), 2.0);
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

        return getRecommendationForFood(user.getPhone(), foodList);
    }

    private static HashMap<String, Double> getRecommendationForFood(String userId, List<String> foodId) {

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

        // Map<Food, Double>
        Map<String, Double> similarityForUserData = new HashMap<>();
        List<String> foodList = new ArrayList<>();
        HashMap<String, Double> similarityResults = new HashMap<>();

        // Todo Filer the food list which current user has rated

        for (String food : foodId) {

            if (!userData.containsKey(food)) {

                foodList.add(food);
            }
        }
        //HashMap<Food, Double>
        for (String food : foodList) {

            HashMap<String, Double> foodData = mSimilarityTable.get(food);

            if (foodData == null) {
                continue;
            }

            // Entry<Food, Double>
            for (Entry<String, Double> itr : userData.entrySet()) {

                // Entry<Food, Double>
                for (Entry<String, Double> itr2 : foodData.entrySet()) {

                    if (itr2.getKey().equals(itr.getKey())) {

                        similarityForUserData.put(itr.getKey(), itr2.getValue());
                    }
                }
            }

            // (4*0.792 + 5*0.8) / (0.792+ 0.8) = 4.5
            Double n = 0.0;
            Double d = 0.0;

            //Entry<Food, Double
            for (Entry<String, Double> itr : userData.entrySet()) {

                //Entry<Food, Double
                for (Entry<String, Double> itr2 : similarityForUserData.entrySet()) {

                    if (itr2.getKey().equals(itr.getKey())) {

                        n = n + (itr.getValue() * itr2.getValue());
                        d = d + itr2.getValue();
                    }
                }
            }

            Double result = n / d;

            similarityResults.put(food, result);

//        System.out.println(String.format("Recommendation for %s for %s is %s",
//                user.userName, book.name, result));

        }

        return similarityResults;

    }
    //endregion
}