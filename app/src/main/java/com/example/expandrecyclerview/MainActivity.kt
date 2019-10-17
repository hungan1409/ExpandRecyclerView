package com.example.expandrecyclerview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.example.expandrecyclerview.databinding.ActivityMainBinding
import com.example.expandrecyclerview.model.Person

//https://blog.untitledkingdom.com/refactoring-recyclerview-adapter-to-data-binding-5631f239095f
//https://medium.com/better-programming/recyclerview-expanded-1c1be424282c

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ExpendableRecyclerViewAdapter
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

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
}
