package poafs.auth;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 3.2.0.
 */
public class ReticulateAuth extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060008054600160a060020a031916331790556119f3806100326000396000f3006080604052600436106100cf5763ffffffff7c01000000000000000000000000000000000000000000000000000000006000350416631ffecafe81146100d4578063211f51f8146101405780632b68b9c614610210578063464232cb146102255780634a632feb14610289578063638ed376146102ff57806363c18a6d146103585780637a941b03146103b1578063c2d7f5611461045e578063c4900141146104b9578063ce82eea914610562578063de9ac3601461060a578063e1d5feb1146106a7578063f1afe04d14610744575b600080fd5b3480156100e057600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e94369492936024939284019190819084018382808284375094975050508335600160a060020a031694505050602090910135905061079d565b005b34801561014c57600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261019b94369492936024939284019190819084018382808284375094975050933594506108679350505050565b6040805160208082528351818301528351919283929083019185019080838360005b838110156101d55781810151838201526020016101bd565b50505050905090810190601f1680156102025780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561021c57600080fd5b5061013e61096b565b34801561023157600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e94369492936024939284019190819084018382808284375094975050509235600160a060020a0316935061098e92505050565b34801561029557600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102ed94369492936024939284019190819084018382808284375094975050509235600160a060020a03169350610d1492505050565b60408051918252519081900360200190f35b34801561030b57600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261019b943694929360249392840191908190840183828082843750949750610d9c9650505050505050565b34801561036457600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102ed943694929360249392840191908190840183828082843750949750610e9e9650505050505050565b3480156103bd57600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261044a94369492936024939284019190819084018382808284375050604080516020601f89358b018035918201839004830284018301909452808352979a9998810197919650918201945092508291508401838280828437509497505093359450610f069350505050565b604080519115158252519081900360200190f35b34801561046a57600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e94369492936024939284019190819084018382808284375094975050933594506111999350505050565b3480156104c557600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526105129436949293602493928401919081908401838280828437509497506112229650505050505050565b60408051602080825283518183015283519192839290830191858101910280838360005b8381101561054e578181015183820152602001610536565b505050509050019250505060405180910390f35b34801561056e57600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e94369492936024939284019190819084018382808284375050604080516020601f818a01358b0180359182018390048302840183018552818452989b600160a060020a038b35169b909a90999401975091955091820193509150819084018382808284375094975050933594506112dc9350505050565b34801561061657600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261044a94369492936024939284019190819084018382808284375050604080516020601f818a01358b0180359182018390048302840183018552818452989b8a359b909a9099940197509195509182019350915081908401838280828437509497506114e39650505050505050565b3480156106b357600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e94369492936024939284019190819084018382808284375050604080516020601f818a01358b0180359182018390048302840183018552818452989b8a359b909a90999401975091955091820193509150819084018382808284375094975061167b9650505050505050565b34801561075057600080fd5b506040805160206004803580820135601f810184900484028501840190955284845261013e9436949293602493928401919081908401838280828437509497506117289650505050505050565b60006001846040518082805190602001908083835b602083106107d15780518252601f1990920191602091820191016107b2565b51815160209384036101000a6000190180199092169116179052920194855250604080519485900382019094203360009081526002820190925293902060010154929350505060031180159061083b57503360009081526002820160205260409020600101548211155b1561086157600160a060020a038316600090815260028201602052604090206001018290555b50505050565b60606001836040518082805190602001908083835b6020831061089b5780518252601f19909201916020918201910161087c565b518151600019602094850361010090810a8201928316921993909316919091179092529490920196875260408051978890038201882060008b8152600390910183528190208054601f600260018316159098029095011695909504928301829004820288018201905281875292945092505083018282801561095e5780601f106109335761010080835404028352916020019161095e565b820191906000526020600020905b81548152906001019060200180831161094157829003601f168201915b5050505050905092915050565b600054600160a060020a031633141561098c57600054600160a060020a0316ff5b565b60006001836040518082805190602001908083835b602083106109c25780518252601f1990920191602091820191016109a3565b51815160209384036101000a60001901801990921691161790529201948552506040805194859003820190942033600090815260028201909252939020600101549293505050600311801590610aa957506001836040518082805190602001908083835b60208310610a455780518252601f199092019160209182019101610a26565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420600160a060020a03871660009081526002918201835285812060019081015433835292880190935294909420015492909210159150505b15610d0f576001836040518082805190602001908083835b60208310610ae05780518252601f199092019160209182019101610ac1565b6001836020036101000a0380198251168184511680821785525050505050509050019150509081526020016040518091039020600201600083600160a060020a0316600160a060020a031681526020019081526020016000206000018054600181600116156101000203166002900490506000141515610d0f576001836040518082805190602001908083835b60208310610b8c5780518252601f199092019160209182019101610b6d565b6001836020036101000a03801982511681845116808217855250505050505090500191505090815260200160405180910390206001016001846040518082805190602001908083835b60208310610bf45780518252601f199092019160209182019101610bd5565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420600160a060020a0388166000908152600291820190925293902090920154835490925082109050610c4e57fe5b600091825260209182902001805473ffffffffffffffffffffffffffffffffffffffff19169055604051845160019286929182918401908083835b60208310610ca85780518252601f199092019160209182019101610c89565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420600160a060020a03871660009081526002909101909152928320929150610cfe9050828261182f565b506000600182018190556002909101555b505050565b60006001836040518082805190602001908083835b60208310610d485780518252601f199092019160209182019101610d29565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420600160a060020a039690961660009081526002909601905250509091206001015492915050565b60606001826040518082805190602001908083835b60208310610dd05780518252601f199092019160209182019101610db1565b518151600019602094850361010090810a82019283169219939093169190911790925294909201968752604080519788900382018820336000908152600291820184528290208054601f600182161590980290950190941604948501829004820288018201905283875290945091925050830182828015610e925780601f10610e6757610100808354040283529160200191610e92565b820191906000526020600020905b815481529060010190602001808311610e7557829003601f168201915b50505050509050919050565b60006001826040518082805190602001908083835b60208310610ed25780518252601f199092019160209182019101610eb3565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922054949350505050565b6000806001856040518082805190602001908083835b60208310610f3b5780518252601f199092019160209182019101610f1c565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092206004015460ff161515915061118c90505760408051606081018252848152815160008152602080820184528083019190915260018284018190529251885192939289928291908401908083835b60208310610fd25780518252601f199092019160209182019101610fb3565b51815160209384036101000a600019018019909216911617905292019485525060405193849003810190932084518155848401518051919461101c94506001860193500190611873565b50604091820151600491909101805460ff19169115159190911790555185516001918791819060208401908083835b6020831061106a5780518252601f19909201916020918201910161104b565b51815160001960209485036101000a019081169019919091161790529201948552506040805194859003820185206001908101805480830180835560009283529185902001805473ffffffffffffffffffffffffffffffffffffffff1916331790556060870183528a875260038785015286830181905291518b519297509094508a9350918291908401908083835b602083106111185780518252601f1990920191602091820191016110f9565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420336000908152600290910182529390932084518051919461116b945085935001906118e5565b50602082015181600101556040820151816002015590505060019150611191565b600091505b509392505050565b60006001836040518082805190602001908083835b602083106111cd5780518252601f1990920191602091820191016111ae565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420336000908152600280830190935294909420600101549394509092109150610d0f9050575550565b60606001826040518082805190602001908083835b602083106112565780518252601f199092019160209182019101611237565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201852060010180548084028701840190925281865293509150830182828015610e9257602002820191906000526020600020905b8154600160a060020a031681526001909101906020018083116112b35750505050509050919050565b6000806001866040518082805190602001908083835b602083106113115780518252601f1990920191602091820191016112f2565b51815160209384036101000a6000190180199092169116179052920194855250604080519485900382019094203360009081526002820190925293902060010154929450505060031180159061137b57503360009081526002830160205260409020600101548311155b156114db576001866040518082805190602001908083835b602083106113b25780518252601f199092019160209182019101611393565b51815160209384036101000a60001901801990921691161790529201948552506040805194859003820185206001908101805480830180835560009283529185902001805473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a038e161790556060870183528a87528684018a905286830181905291518c519297509094508b9350918291908401908083835b602083106114675780518252601f199092019160209182019101611448565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420600160a060020a038b16600090815260029091018252939093208451805191946114c3945085935001906118e5565b50602082015160018201556040909101516002909101555b505050505050565b60008060006001866040518082805190602001908083835b6020831061151a5780518252601f1990920191602091820191016114fb565b6001836020036101000a038019825116818451168082178552505050505050905001915050908152602001604051809103902060030160008681526020019081526020016000209150818054600181600116156101000203166002900490508451141561166d575060005b8154600260001961010060018416150201909116048110156116645783818151811015156115af57fe5b90602001015160f860020a900460f860020a027effffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff19168282815460018160011615610100020316600290048110151561160357fe5b8154600116156116225790600052602060002090602091828204019190065b9054901a60f860020a027fff00000000000000000000000000000000000000000000000000000000000000161461165c5760009250611672565b600101611585565b60019250611672565b600092505b50509392505050565b60006001846040518082805190602001908083835b602083106116af5780518252601f199092019160209182019101611690565b51815160209384036101000a600019018019909216911617905292019485525060408051948590038201909420336000908152600280830190935294909420600101549394509092109150610861905057600083815260038201602090815260409091208351611721928501906118e5565b5050505050565b60036001826040518082805190602001908083835b6020831061175c5780518252601f19909201916020918201910161173d565b51815160209384036101000a6000190180199092169116179052920194855250604080519485900382019094203360009081526002909101909152929092206001015492909210915061182c9050576001816040518082805190602001908083835b602083106117dd5780518252601f1990920191602091820191016117be565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092206000808255909250905061181f600183018261195f565b50600401805460ff191690555b50565b50805460018160011615610100020316600290046000825580601f10611855575061182c565b601f01602090049060005260206000209081019061182c9190611979565b8280548282559060005260206000209081019282156118d5579160200282015b828111156118d5578251825473ffffffffffffffffffffffffffffffffffffffff1916600160a060020a03909116178255602090920191600190910190611893565b506118e1929150611996565b5090565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f1061192657805160ff1916838001178555611953565b82800160010185558215611953579182015b82811115611953578251825591602001919060010190611938565b506118e1929150611979565b508054600082559060005260206000209081019061182c91905b61199391905b808211156118e1576000815560010161197f565b90565b61199391905b808211156118e157805473ffffffffffffffffffffffffffffffffffffffff1916815560010161199c5600a165627a7a72305820c55efc35ccdab0892096ab663620195054b929041d88656ae10150f86d5423320029";

    protected ReticulateAuth(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ReticulateAuth(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<TransactionReceipt> modifyAccessLevel(String fileId, String user, BigInteger level) {
        Function function = new Function(
                "modifyAccessLevel", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(user), 
                new org.web3j.abi.datatypes.generated.Uint256(level)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<byte[]> getCheckSum(String fileId, BigInteger blockIndex) {
        Function function = new Function("getCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<TransactionReceipt> destruct() {
        Function function = new Function(
                "destruct", 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> revokeShare(String fileId, String revokee) {
        Function function = new Function(
                "revokeShare", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(revokee)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<BigInteger> getAccessLevel(String fileId, String user) {
        Function function = new Function("getAccessLevel", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(user)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<byte[]> getKeyForFile(String fileId) {
        Function function = new Function("getKeyForFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public RemoteCall<BigInteger> getFileLength(String fileId) {
        Function function = new Function("getFileLength", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteCall<TransactionReceipt> addFile(String fileId, byte[] key, BigInteger length) {
        Function function = new Function(
                "addFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.DynamicBytes(key), 
                new org.web3j.abi.datatypes.generated.Uint256(length)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> updateFileLength(String fileId, BigInteger newLength) {
        Function function = new Function(
                "updateFileLength", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(newLength)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<List<String>> getAllUsersWithAccess(String fileId) {
        Function function = new Function("getAllUsersWithAccess", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicArray<Address>>() {}));
        return executeRemoteCallSingleValueReturn(function, List<String>.class);
    }

    public RemoteCall<TransactionReceipt> shareFile(String fileId, String recipient, byte[] recipientKey, BigInteger level) {
        Function function = new Function(
                "shareFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.Address(recipient), 
                new org.web3j.abi.datatypes.DynamicBytes(recipientKey), 
                new org.web3j.abi.datatypes.generated.Uint256(level)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<Boolean> compareCheckSum(String fileId, BigInteger blockIndex, byte[] checkSum) {
        Function function = new Function("compareCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex), 
                new org.web3j.abi.datatypes.DynamicBytes(checkSum)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<TransactionReceipt> updateCheckSum(String fileId, BigInteger blockIndex, byte[] checkSum) {
        Function function = new Function(
                "updateCheckSum", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId), 
                new org.web3j.abi.datatypes.generated.Uint256(blockIndex), 
                new org.web3j.abi.datatypes.DynamicBytes(checkSum)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<TransactionReceipt> removeFile(String fileId) {
        Function function = new Function(
                "removeFile", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(fileId)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public static RemoteCall<ReticulateAuth> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateAuth.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ReticulateAuth> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateAuth.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static ReticulateAuth load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateAuth(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ReticulateAuth load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateAuth(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
