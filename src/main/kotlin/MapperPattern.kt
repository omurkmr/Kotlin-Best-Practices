fun main(args: Array<String>) {
    println("Mapper Pattern")

    //Lets fetch data from somewhere (db, remote etc.)
    val addressDb = object : AppDatabase {
        //returns some mock data
        override fun getAddress(): DataAddress = DataAddress("1", "FancyStreet", "1", "CoolCity")
    }

    //inject dataSource and mapper, this wat we can test way more easily then using extensions. Just have to create mock impl
    val addressRepository = AddressRepositoryImpl(AddressDataSourceImpl(addressDb), DataAddressToDomainAddressMapper())

    //We have the data so next step is to transform data to something useful to user. I decide(also preferred way) to use Model–view–viewModel(MVVM) pattern for it.
    val useCase = GetAddressUseCaseImpl(addressRepository)
    val addressViewModel = AddressViewModel(useCase, DomainAddressToAddressMapper())

    //finally, lets jump into showing UI step. We only expose presentation part to view. You can think View as a Fragment/Android for android.
    val view = View(addressViewModel)
    view.displayAddressData()
}

// Presentation Model
data class Address(
    val fullAddress: String
)

// Domain Model
data class DomainAddress(
    val id: String,
    val streetName: String,
    val streetNumber: String,
    val city: String
) {
    val fullAddress: String get() = "$streetName $streetNumber\n$city"
}

// Data Model
data class DataAddress(
    val use_id: String,
    val use_street_name: String,
    val use_number: String,
    val use_town: String
)

// Presentation
class View(
    private val addressViewModel: AddressViewModel
) {
    fun displayAddressData() {
        val address = addressViewModel.address
        print(address)
    }
}

class AddressViewModel(
    private val getAddressUseCase: GetAddressUseCase,
    private val domainAddressToAddressMapper: DomainAddressToAddressMapper,
) {
    val address: Address get() = getAddressUseCase().let(domainAddressToAddressMapper::map)
}

class DomainAddressToAddressMapper : Mapper<DomainAddress, Address> {
    override fun map(input: DomainAddress): Address = with(input) {
        Address(fullAddress)
    }
}

// Domain
interface GetAddressUseCase {
    operator fun invoke(): DomainAddress
}

class GetAddressUseCaseImpl(
    private val addressRepository: AddressRepository
) : GetAddressUseCase {
    override fun invoke(): DomainAddress = addressRepository.getAddress()
}

interface AddressRepository {
    fun getAddress(): DomainAddress
}

class AddressRepositoryImpl(
    private val addressDataSource: AddressDataSource,
    private val dataAddressToDomainAddressMapper: DataAddressToDomainAddressMapper,
) : AddressRepository {
    override fun getAddress(): DomainAddress = addressDataSource.getAddress().let(dataAddressToDomainAddressMapper::map)
}

class DataAddressToDomainAddressMapper : Mapper<DataAddress, DomainAddress> {
    override fun map(input: DataAddress): DomainAddress = with(input) {
        DomainAddress(use_id, use_street_name, use_number, use_town)
    }
}

// Data
interface AddressDataSource {
    fun getAddress(): DataAddress
}

class AddressDataSourceImpl(
    private val appDatabase: AppDatabase,
) : AddressDataSource {
    override fun getAddress(): DataAddress =
        appDatabase.getAddress()
}

interface AppDatabase {
    fun getAddress(): DataAddress
}

interface Mapper<in I, out O> {
    fun map(input: I): O
}

interface ListMapper<in I, out O> : Mapper<List<I>, List<O>>

//If you need a list of models, consider to use this ListMapperImpl class that receive a mapper to do the conversion.
class ListMapperImpl<in I, out O>(
    private val mapper: Mapper<I, O>
) : ListMapper<I, O> {
    override fun map(input: List<I>): List<O> =
        input.map(mapper::map)
}
