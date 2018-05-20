package com.codelab.mlkitcodelab

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.LinearSnapHelper
import com.codelab.mlkitcodelab.adapters.PagesAdapter
import com.codelab.mlkitcodelab.utils.AppConstants
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val mFirestoreReference by lazy {
        FirebaseFirestore.getInstance().collection(AppConstants.DATABASE_COLLECTION)
    }

    private val mWebFirebaseRef by lazy {
        FirebaseFirestore.getInstance().collection("web")
    }

    private val mRecyclerViewHorizontalLayout by lazy {
        LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
    }

    private val mPagesList: ArrayList<String> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvList.layoutManager = mRecyclerViewHorizontalLayout
        LinearSnapHelper().attachToRecyclerView(rvList)

        //If reverse order required than use Query.Direction.DESCENDING
        mFirestoreReference.orderBy("Timestamp")
                .addSnapshotListener { querySnapshot, _ ->
                    if (querySnapshot != null && querySnapshot.isEmpty.not() && querySnapshot.metadata.hasPendingWrites().not()) {
                        for (documents in querySnapshot.documentChanges) {
                            mPagesList.add(documents?.document?.get("Text").toString())
                        }
                        rvList.adapter = PagesAdapter(mPagesList)
                        rvList.scrollToPosition(mPagesList.size.dec())
                    }
                }

        //Start text recognition when user press label button
        tvLabel.setOnClickListener {
            startActivity(Intent(this@MainActivity, RecognizeTextActivity::class.java))
        }

        tvBrowser.setOnClickListener {
            if (mRecyclerViewHorizontalLayout.findFirstVisibleItemPosition() < mPagesList.size) {
                mWebFirebaseRef.document("current")
                        .set(mapOf("Text" to mPagesList[mRecyclerViewHorizontalLayout.findFirstVisibleItemPosition()]))
                        .addOnCompleteListener { }
            }
        }
    }
}
