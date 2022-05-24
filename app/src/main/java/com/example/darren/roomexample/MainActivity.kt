package com.example.darren.roomexample

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.darren.roomexample.databinding.ActivityMainBinding
import com.example.darren.roomexample.databinding.DialogUpdateBinding
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private var binding: ActivityMainBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        val employeeDao = (application as EmployeeApp).db.employeeDao()
        binding?.btnAdd?.setOnClickListener {
            addRecord(employeeDao)
        }

        lifecycleScope.launch {
            employeeDao.fetchAllEmployees().collect {
                val list = ArrayList(it)
                setUpListOfDataIntoRecyclerView(list, employeeDao)
            }
        }

    }

    private fun addRecord(employeeDao: EmployeeDao){
        val name = binding?.etName?.text.toString()
        val email = binding?.etEmailId?.text.toString()
        if(name.isNotEmpty() && email.isNotEmpty()){
            lifecycleScope.launch{
                employeeDao.insert(EmployeeEntity(name=name, email=email))
                Toast.makeText(applicationContext, "Record saved", Toast.LENGTH_LONG).show()
                binding?.etName?.text?.clear()
                binding?.etEmailId?.text?.clear()
            }
        } else{
            Toast.makeText(applicationContext, "Name or Email can not be blank", Toast.LENGTH_LONG).show()
        }
    }

    private fun setUpListOfDataIntoRecyclerView(employeeList: ArrayList<EmployeeEntity>,
                                                employeeDao: EmployeeDao){
        if (employeeList.isNotEmpty()){
            val itemAdapter = ItemAdapter(employeeList,
                                {
                                    updateId ->
                                    updateRecordDialog(updateId, employeeDao)
                                },
                                {
                                    deleteId ->
                                    deleteRecordDialog(deleteId, employeeDao)
                                }
            )
            binding?.rvItemsList?.layoutManager = LinearLayoutManager(this)
            binding?.rvItemsList?.adapter = itemAdapter
            binding?.rvItemsList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
        } else{
            binding?.rvItemsList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }
    }

    private fun updateRecordDialog(id: Int, employeeDao: EmployeeDao){
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        val updateDialogBinding = DialogUpdateBinding.inflate(layoutInflater)
        updateDialog.setContentView(updateDialogBinding.root)

        lifecycleScope.launch {
            employeeDao.fetchEmployeeById(id).collect {
                if(it != null){
                    updateDialogBinding.etUpdateName.setText(it.name)
                    updateDialogBinding.etUpdateEmailId.setText(it.email)
                }
            }
        }

        updateDialogBinding.tvUpdate.setOnClickListener {
            val name = updateDialogBinding.etUpdateName.text.toString()
            val email = updateDialogBinding.etUpdateEmailId.text.toString()
            if(name.isNotEmpty() && email.isNotEmpty()){
                lifecycleScope.launch{
                    employeeDao.update(EmployeeEntity(id, name, email))
                    Toast.makeText(applicationContext, "Record Updated", Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                }
            } else{
                Toast.makeText(applicationContext, "Name and email can not be blank", Toast.LENGTH_LONG).show()
            }
        }

        updateDialogBinding.tvCancel.setOnClickListener{
            updateDialog.dismiss()
        }
        updateDialog.show()
    }

    private fun deleteRecordDialog(id: Int, employeeDao: EmployeeDao) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Record")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        lifecycleScope.launch{
            employeeDao.fetchEmployeeById(id).collect {
                if(it != null){
                    builder.setMessage("Are you sure you want to delete ${it.name}")
                }
            }
        }

        builder.setPositiveButton("YES"){dialogInterface, _ ->
            lifecycleScope.launch {
                employeeDao.delete(EmployeeEntity(id))
                Toast.makeText(applicationContext, "Record delete successfully.", Toast.LENGTH_LONG).show()
            }
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("NO"){dialogInterface, _ ->
            dialogInterface.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.setCancelable(false)
        alertDialog.show()


    }
}