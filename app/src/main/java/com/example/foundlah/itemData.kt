package com.example.foundlah

import android.os.Parcel
import android.os.Parcelable

data class ItemData(
    val name: String?,
    val category: String?,
    val date: String?,
    val location: String?,
    val description: String?,
    val imageBase64: String?,
    val type: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeString(location)
        parcel.writeString(description)
        parcel.writeString(imageBase64)
        parcel.writeString(type)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ItemData> {
        override fun createFromParcel(parcel: Parcel): ItemData {
            return ItemData(parcel)
        }

        override fun newArray(size: Int): Array<ItemData?> {
            return arrayOfNulls(size)
        }
    }
}
