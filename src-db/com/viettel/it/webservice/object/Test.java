package com.viettel.it.webservice.object;

import com.viettel.it.util.PasswordEncoder;

/**
 * Created by quytv7 on 8/11/2018.
 */
public class Test {
    public static void main(String[] args) {
        try {
            String a = PasswordEncoder.decrypt("aNXwe7427MfI7PplhiCtwQu+SPBqRK6qF9nHxeiCYu1d4g85G9EpciKrKJ2wgkvor5GueiSni2oG\n" +
                    "xyd2iHxFrtAJvZiY7gn+4VGrJ9BIu0gFYyS3/AjOOJI7L6jwz3lJnZMvnZapVKZTd1wFpPA3t4hO\n" +
                    "nB72OfmqLiO5eS7wsY3N/KT0y6pupddJXIto3+56k+ehC98kbAPerVeuim8TkNSjgoS5Sp6v7Y7R\n" +
                    "1k3ibRY=");
            System.out.println(a);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
