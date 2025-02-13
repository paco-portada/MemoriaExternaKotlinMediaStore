package com.example.memoriaexternakotlin

import android.os.Environment
import android.os.Environment.DIRECTORY_DOCUMENTS
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getExternalFilesDirs
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Memoria {
    @JvmStatic
    @Throws(IOException::class)
    fun escribirExterna(fichero: String, cadena: String): Boolean {
        val tarjeta: File = Environment.getExternalStorageDirectory()
        // val tarjeta1: Array<out File> = getExternalFilesDirs(DIRECTORY_DOCUMENTS)
        val miFichero = File(tarjeta.absolutePath, fichero)

        //tarjeta = Environment.getExternalStoragePublicDirectory("datos/programas/");
        //tarjeta.mkdirs();

        return escribir(miFichero, cadena)
    }

    @Throws(IOException::class)
    private fun escribir(fichero: File, cadena: String): Boolean {
        val correcto = true
        val bufferedWriter = fichero.bufferedWriter()
        bufferedWriter.write(cadena)
        bufferedWriter.close()

        return correcto
        /*
        lateinit var fos: FileOutputStream
        lateinit var osw: OutputStreamWriter
        lateinit var out: BufferedWriter
        val correcto = true

        fos = FileOutputStream(fichero)
        osw = OutputStreamWriter(fos)
        out = BufferedWriter(osw)
        out.write(cadena)
        out.close()

        return correcto
         */
    }

    fun mostrarPropiedades(fichero: File): String {
        var formato: SimpleDateFormat = SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault())
        val txt = StringBuffer()

        try {
            if (fichero.exists()) {
                txt.append(fichero.name + '\n')
                txt.append(fichero.absolutePath + '\n')
                txt.append("Tamaño (bytes): " + fichero.length() + '\n')
                txt.append("Fecha: " + formato.format(Date(fichero.lastModified())))
            } else
                txt.append("No existe el fichero " + fichero.name)
        } catch (e: Exception) {
            Log.e("Error", e.message.toString())
            txt.append(e.message)
        }

        return txt.toString()
    }

    @JvmStatic
    fun mostrarPropiedadesExterna(fichero: String): String {

        val tarjeta: File = Environment.getExternalStorageDirectory()
        val miFichero = File(tarjeta.absolutePath, fichero)

        return mostrarPropiedades(miFichero)
    }

    @JvmStatic
    fun disponibleEscritura(): Boolean {
        var escritura = false

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        val estado = Environment.getExternalStorageState()
        if (estado == Environment.MEDIA_MOUNTED) escritura = true

        return escritura
    }

    fun disponibleLectura(): Boolean {
        var lectura = false

        //Comprobamos el estado de la memoria externa (tarjeta SD)
        val estado = Environment.getExternalStorageState()
        if (estado == Environment.MEDIA_MOUNTED_READ_ONLY || estado == Environment.MEDIA_MOUNTED) lectura = true

        return lectura
    }

    @JvmStatic
    @Throws(IOException::class)
    fun leerExterna(fichero: String?): String {
        val miFichero: File
        val tarjeta: File

        //tarjeta = Environment.getExternalStoragePublicDirectory("datos/programas/");
        tarjeta = Environment.getExternalStorageDirectory()
        miFichero = File(tarjeta.absolutePath, fichero)

        return leer(miFichero)
    }

    @Throws(IOException::class)
    private fun leer(fichero: File): String {

        val inputString = fichero.bufferedReader ().use {
            it.readText ()
        }
        fichero.bufferedReader().close()

        return inputString


        /*
        lateinit var fis: FileInputStream
        lateinit var isw: InputStreamReader
        lateinit var br: BufferedReader
        lateinit var linea: String
        val miCadena = StringBuilder()

        fis = FileInputStream(fichero)
        isw = InputStreamReader(fis)
        br = BufferedReader(isw)
        //while ((n = in.read()) != -1)
        //    miCadena.append((char) n);
        linea = br.readLine()
        while ( linea != null) {
            miCadena.append(linea).append('\n')
            linea = br.readLine()
        }
        br.close()

        return miCadena.toString()
         */
    }
}