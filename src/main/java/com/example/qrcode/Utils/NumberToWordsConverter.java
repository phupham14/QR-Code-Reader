package com.example.qrcode.Utils;

import java.util.ArrayList;
import java.util.List;

public class NumberToWordsConverter {
    private final String[] units = {
            "", "một", "hai", "ba", "bốn", "năm",
            "sáu", "bảy", "tám", "chín"
    };

    private String readThreeDigits(int number, boolean forceFullRead) {
        int hundred = number / 100;
        int tenUnit = number % 100;
        int ten = tenUnit / 10;
        int unit = tenUnit % 10;

        StringBuilder sb = new StringBuilder();

        if (hundred > 0) {
            sb.append(units[hundred]).append(" trăm");
        } else if (forceFullRead && (ten > 0 || unit > 0)) {
            sb.append("không trăm");
        }

        if (ten > 1) {
            sb.append(" ").append(units[ten]).append(" mươi");
            if (unit == 1) {
                sb.append(" mốt");
            } else if (unit == 5) {
                sb.append(" lăm");
            } else if (unit == 4) {
                sb.append(" tư");
            } else if (unit > 0) {
                sb.append(" ").append(units[unit]);
            }
        } else if (ten == 1) {
            sb.append(" mười");
            if (unit == 1) {
                sb.append(" một");
            } else if (unit == 5) {
                sb.append(" lăm");
            } else if (unit > 0) {
                sb.append(" ").append(units[unit]);
            }
        } else if (ten == 0 && unit > 0) {
            if (hundred > 0 || forceFullRead) {
                sb.append(" lẻ ");
                if (unit == 4) {
                    sb.append("tư");
                } else {
                    sb.append(units[unit]);
                }
            } else {
                if (unit == 4) {
                    sb.append("tư");
                } else {
                    sb.append(units[unit]);
                }
            }
        }

        return sb.toString().trim();
    }

    public String convert(long number) {
        if (number == 0) return "không đồng";

        String[] unitNames = {"", " nghìn", " triệu", " tỷ", " nghìn tỷ", " triệu tỷ"};
        StringBuilder result = new StringBuilder();

        List<Integer> groups = new ArrayList<>();
        while (number > 0) {
            groups.add((int) (number % 1000));
            number /= 1000;
        }

        boolean hasGroupBefore = false;

        for (int i = groups.size() - 1; i >= 0; i--) {
            int group = groups.get(i);
            boolean forceFullRead = false;

            if (group > 0) {
                // Nhóm hiện tại < 100 và đã có nhóm trước thì cần đọc đủ
                if (hasGroupBefore && group < 100) {
                    forceFullRead = true;
                }

                String groupText = readThreeDigits(group, forceFullRead);
                result.append(groupText).append(unitNames[i]).append(" ");
                hasGroupBefore = true;
            } else {
                // Nếu nhóm này bằng 0 nhưng sau nó (phía bên phải) còn nhóm > 0
                // và đã có nhóm trước, thì nhóm hiện tại cần giữ ngữ cảnh
                if (hasGroupBefore && hasNonZeroAfter(groups, i)) {
                    result.append("không trăm").append(unitNames[i]).append(" ");
                }
            }
        }

        return "bạn đã chuyển " + result.toString().trim().replaceAll("\\s+", " ") + " đồng";
    }

    private boolean hasNonZeroAfter(List<Integer> groups, int currentIndex) {
        for (int j = currentIndex - 1; j >= 0; j--) {
            if (groups.get(j) != 0) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        NumberToWordsConverter converter = new NumberToWordsConverter();
        String result = converter.convert(15000);
        System.out.println(result);
    }

}
