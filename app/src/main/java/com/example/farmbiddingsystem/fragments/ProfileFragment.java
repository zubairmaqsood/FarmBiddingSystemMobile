    package com.example.farmbiddingsystem.fragments;

    import android.content.Intent;
    import android.os.Bundle;

    import androidx.fragment.app.Fragment;

    import android.view.LayoutInflater;
    import android.view.View;
    import android.view.ViewGroup;
    import android.widget.TextView;

    import com.example.farmbiddingsystem.LoginActivity;
    import com.example.farmbiddingsystem.R;
    import com.example.farmbiddingsystem.utils.AuctionDataHolder;
    import com.example.farmbiddingsystem.utils.SharedPrefManager;

    /**
     * A simple {@link Fragment} subclass.
     * Use the {@link ProfileFragment#newInstance} factory method to
     * create an instance of this fragment.
     */
    public class ProfileFragment extends Fragment {

        // TODO: Rename parameter arguments, choose names that match
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private static final String ARG_PARAM1 = "param1";
        private static final String ARG_PARAM2 = "param2";

        // TODO: Rename and change types of parameters
        private String mParam1;
        private String mParam2;

        public ProfileFragment() {
            // Required empty public constructor
        }

        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        public static ProfileFragment newInstance(String param1, String param2) {
            ProfileFragment fragment = new ProfileFragment();
            Bundle args = new Bundle();
            args.putString(ARG_PARAM1, param1);
            args.putString(ARG_PARAM2, param2);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getArguments() != null) {
                mParam1 = getArguments().getString(ARG_PARAM1);
                mParam2 = getArguments().getString(ARG_PARAM2);
            }
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_profile, container, false);

            // Find the Logout TextView
            TextView btnLogout = view.findViewById(R.id.btnLogout);

            // Set the Click Listener
            btnLogout.setOnClickListener(v -> {
                // 1. Clear SharedPreferences (Token & Role)
                SharedPrefManager prefManager = new SharedPrefManager(requireActivity());
                prefManager.logout();

                // 2. Clear Cached Data so the next user doesn't see old data
                AuctionDataHolder.getInstance().clearAllData();

                // 3. Redirect to Login Activity and clear the backstack
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                // These flags prevent the user from pressing 'Back' to return to the profile
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            });

            return view;
        }
    }