package com.example.expandrecyclerview

import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.databinding.DataBindingUtil
import androidx.datastore.CorruptionException
import androidx.datastore.DataStore
import androidx.datastore.Serializer
import androidx.datastore.createDataStore
import androidx.datastore.preferences.Preferences
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.edit
import androidx.datastore.preferences.preferencesKey
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.example.expandrecyclerview.databinding.ActivityMainBinding
import com.example.expandrecyclerview.model.Person
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.Executors

//https://blog.untitledkingdom.com/refactoring-recyclerview-adapter-to-data-binding-5631f239095f
//https://medium.com/better-programming/recyclerview-expanded-1c1be424282c

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ExpendableRecyclerViewAdapter
    private lateinit var binding: ActivityMainBinding

    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo


    // declare value for working with datastore
    lateinit var dataStore: DataStore<Preferences>

    //create key for save data type
    val PREF_UUID = preferencesKey<String>(name = "uuid")

    // declare value for receive data
    lateinit var uuid: Flow<String>

    //create proto data store
    lateinit var protoDataStore: DataStore<TestModel>
    lateinit var token: Flow<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //create preference data store
        dataStore = this.createDataStore(name = "MyPreferenceDataStore")

        //create proto data store
        protoDataStore = this.createDataStore(
            fileName = "MyProtoDataStore.proto",
            serializer = MyProtoDataStoreSerializer
        )

        readUUID()
        CoroutineScope(Dispatchers.Main).launch {
            uuid.collect { value ->
                if (value.isEmpty()) {
                    val newUUID = UUID.randomUUID().toString()
                    showToast("Not have UUID saved. Create new UUID ${newUUID} and save to Preferences DataStore")
                    delay(5000)
                    saveUUID(newUUID)

                } else {
                    showToast("Read UUID from preference data store ${value}")
                }
            }
        }

        readToken()
        CoroutineScope(Dispatchers.Main).launch {
            token.collect { value ->
                if (value.isEmpty()) {
                    val newToken = "Your token ABCDEF"
                    showToast("Not have token saved. Create new token ${newToken} and save to Proto DataStore")
                    delay(50000)
                    saveToken(newToken)

                } else {
                    showToast("Read the token from proto data store ${value}")
                }
            }
        }

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        showListPerson()

//        checkAuthentication()

        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    private fun checkAuthentication() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate()) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                showAuthentication()
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                showToast("Biometric features no support with current hardware.")
            //Use password authentication in this case
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                showToast("Biometric features are currently unavailable.")
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                showToast("Please associate a biometric credential with your account.")
            else ->
                showToast("An unknown error occurred. Please check your Biometric settings")
        }
    }

    private fun showToast(s: String) {
        this.runOnUiThread {
            val toast = Toast.makeText(this, s, Toast.LENGTH_LONG)
            toast.setGravity(Gravity.CENTER, 0, 0)
            toast.show()
        }
    }

    private fun showAuthentication() {
        val executor = Executors.newSingleThreadExecutor()
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(
                    errorCode: Int,
                    errString: CharSequence
                ) {
                    super.onAuthenticationError(errorCode, errString)
                    runOnUiThread {
                        showToast("Authentication error: $errString")
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    runOnUiThread {
                        showToast("Authentication failed")
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {// 2
                    super.onAuthenticationSucceeded(result)

                    runOnUiThread {
                        showToast("Authentication succeeded!")
                        showListPerson()
                    }
                }
            })


        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric login for your app")
            .setSubtitle("Log in using your biometric credential")
            .setDeviceCredentialAllowed(true) // 4
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showListPerson() {
        adapter = ExpendableRecyclerViewAdapter(getListPersonFake())
        binding.recyclerPerson.adapter = adapter
        binding.recyclerPerson.itemAnimator = null
    }

    fun getListPersonFake(): List<Person> {
        val people = mutableListOf<Person>()

        val personNames = resources.getStringArray(R.array.people)
        val images = resources.obtainTypedArray(R.array.images)

        val generalDescription = resources.getString(R.string.description)
        personNames.forEachIndexed { index, personName ->
            val person = Person(
                personName,
                generalDescription,
                images.getResourceId(index, -1),
                false
            )
            people.add(person)
        }

        return people
    }

    fun getDescription(): String {
        val description: String = """
            1. Better safe than sorry – Cẩn tắc vô áy náy
            
            2. Money is a good servant but a bad master – Khôn lấy của che thân, dại lấy thân che của
            
            3. The grass are always green on the other side of the fence – Đứng núi này trông núi nọ
            
            4. Once bitten, twice shy – Chim phải đạn sợ cành cong
            
            5. When in Rome (do as the Romans do) – Nhập gia tùy tục
            
            6. Honesty is the best policy – Thật thà là thượng sách
            
            7. A woman gives and forgives, a man gets and forgets – Đàn bà cho và tha thứ, đàn ông nhận và quên
            
            8. No rose without a thorn – Hồng nào mà chẳng có gai, việc nào mà chẳng có vài khó khăn!
            
            9. Save for a rainy day – Làm khi lành để dành khi đau
            
            10. It’s an ill bird that fouls its own nest – Vạch áo cho người xem lưng/ Tốt đẹp phô ra xấu xa đậy lại.
        """.trimIndent()

        return description
    }

    suspend fun saveUUID(uuid: String) {
        dataStore.edit {
            it[PREF_UUID] = uuid
        }
    }

    fun readUUID() {
        uuid = dataStore.data.map {
            it[PREF_UUID] ?: ""
        }
    }

    object MyProtoDataStoreSerializer : Serializer<TestModel> {
        override fun readFrom(input: InputStream): TestModel {
            try {
                return TestModel.parseFrom(input)
            } catch (exception: InvalidProtocolBufferException) {
                throw CorruptionException("Cannot read proto.", exception)
            }
        }

        override fun writeTo(t: TestModel, output: OutputStream) = t.writeTo(output)
    }


    suspend fun saveToken(token: String) {
        protoDataStore.updateData {
            it.toBuilder().setToken(token).build()
        }
    }

    fun readToken() {
        token = protoDataStore.data.map {
            it.token
        }
    }
}


