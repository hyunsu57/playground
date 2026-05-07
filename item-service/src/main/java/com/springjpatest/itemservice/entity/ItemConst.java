package com.springjpatest.itemservice.entity;

public class ItemConst {
    public final static String ALL = "all";

    public enum ItemSort {
        ALL("모두",null),
        RegDateMin("오래된순", "asc"),
        RegDateMax("최신순", "desc"),
        PriceMin("높은 가격", "desc"),
        PriceMax("낮은 가격", "asc");

        private final String name;
        private final String value;

        ItemSort(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public String getName() {
            return name;
        }
    }

    public enum ItemType {
        ALL("모두", "all"),
        GAME_MONEY("돈", "money"),
        ITEM("아이템", "item"),
        ACCOUNT("계정", "account"),
        ETC("기타", "etc");

        private final String name;
        private final String value;

        ItemType(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
}
