package com.test.maxp36.ytranslator;



public class Key {
    private String key;
    public Key(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
    /*@Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                myKey myKey = (myKey) o;

                return key != null ? key.equals(myKey.key) : myKey.key == null;
                if (key == null) {
                    if (myKey.getKey() != null) return false;
                } else if (!key.equals(myKey.getKey()))
                    return false;
                return true;

            }

            @Override
            public int hashCode() {
                return key != null ? key.hashCode() : 0;
            }*/
}
