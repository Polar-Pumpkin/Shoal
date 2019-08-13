package net.shoal.sir.voteup.enums;

import java.util.ArrayList;
import java.util.List;

public enum Position {
    x1, x2, x3, x4, x5, x6, x7 ,x8 ,x9,
    y1, y2, y3, y4, y5, y6;

    public static List<String> resolutionLocation(String positionString) {
        List<String> list = new ArrayList<>();

        if(positionString.contains(",")) {
            for(String interval : positionString.split(",")) {
                list.addAll(calculateInterval(interval));
            }
        } else if(positionString.contains("-")) {
            list.addAll(calculateInterval(positionString));
        } else {
            list.add(positionString);
        }

        return list;
    }

    public static List<String> calculateInterval(String interval) {
        List<String> list = new ArrayList<>();
        if(interval.contains("-")) {
            String[] set = interval.split("-");
            for(int i = Integer.parseInt(set[0]); i <= Integer.parseInt(set[1]); i++) {
                list.add(String.valueOf(i));
            }
        } else {
            list.add(interval);
        }
        return list;
    }

    public static List<Integer> getPositionList(String x, String y) {
        List<Integer> list = new ArrayList<>();
        for(String ySlot : resolutionLocation(y)) {
            for(String xSlot : resolutionLocation(x)) {
                list.add(getPosition(Integer.parseInt(xSlot), Integer.parseInt(ySlot)));
            }
        }
        return list;
    }

    public static int getPosition(int xSlot, int ySlot) {
        Position x = valueOf("x" + xSlot);
        Position y = valueOf("y" + ySlot);
        switch(y) {
            case y1:
                switch(x) {
                    case x1:
                        return 0;
                    case x2:
                        return 1;
                    case x3:
                        return 2;
                    case x4:
                        return 3;
                    case x5:
                        return 4;
                    case x6:
                        return 5;
                    case x7:
                        return 6;
                    case x8:
                        return 7;
                    case x9:
                        return 8;
                    default:
                        return 0;
                }
            case y2:
                switch(x) {
                    case x1:
                        return 9;
                    case x2:
                        return 10;
                    case x3:
                        return 11;
                    case x4:
                        return 12;
                    case x5:
                        return 13;
                    case x6:
                        return 14;
                    case x7:
                        return 15;
                    case x8:
                        return 16;
                    case x9:
                        return 17;
                    default:
                        return 0;
                }
            case y3:
                switch(x) {
                    case x1:
                        return 18;
                    case x2:
                        return 19;
                    case x3:
                        return 20;
                    case x4:
                        return 21;
                    case x5:
                        return 22;
                    case x6:
                        return 23;
                    case x7:
                        return 24;
                    case x8:
                        return 25;
                    case x9:
                        return 26;
                    default:
                        return 0;
                }
            case y4:
                switch(x) {
                    case x1:
                        return 27;
                    case x2:
                        return 28;
                    case x3:
                        return 29;
                    case x4:
                        return 30;
                    case x5:
                        return 31;
                    case x6:
                        return 32;
                    case x7:
                        return 33;
                    case x8:
                        return 34;
                    case x9:
                        return 35;
                    default:
                        return 0;
                }
            case y5:
                switch(x) {
                    case x1:
                        return 36;
                    case x2:
                        return 37;
                    case x3:
                        return 38;
                    case x4:
                        return 39;
                    case x5:
                        return 40;
                    case x6:
                        return 41;
                    case x7:
                        return 42;
                    case x8:
                        return 43;
                    case x9:
                        return 44;
                    default:
                        return 0;
                }
            case y6:
                switch(x) {
                    case x1:
                        return 45;
                    case x2:
                        return 46;
                    case x3:
                        return 47;
                    case x4:
                        return 48;
                    case x5:
                        return 49;
                    case x6:
                        return 50;
                    case x7:
                        return 51;
                    case x8:
                        return 52;
                    case x9:
                        return 53;
                    default:
                        return 0;
                }
            default:
                return 0;
        }
    }
}
