package xyz.reticulate.auth;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.DynamicBytes;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
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
public class ReticulateUsers extends Contract {
    private static final String BINARY = "608060405234801561001057600080fd5b5060008054600160a060020a033316600160a060020a03199091161790556109628061003d6000396000f3006080604052600436106100985763ffffffff7c010000000000000000000000000000000000000000000000000000000060003504166314ffa2c2811461009d5780632b68b9c61461016b5780633d4a068d1461018257806399a4b08f146101ef578063b7f2749414610210578063c5b76ff214610285578063de4f1a2a146102a6578063df4a91451461037b578063f0322088146103d4575b600080fd5b3480156100a957600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100f69436949293602493928401919081908401838280828437509497506103f59650505050505050565b6040805160208082528351818301528351919283929083019185019080838360005b83811015610130578181015183820152602001610118565b50505050905090810190601f16801561015d5780820380516001836020036101000a031916815260200191505b509250505060405180910390f35b34801561017757600080fd5b50610180610414565b005b34801561018e57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101db94369492936024939284019190819084018382808284375094975061043b9650505050505050565b604080519115158252519081900360200190f35b3480156101fb57600080fd5b506100f6600160a060020a03600435166104be565b34801561021c57600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526102699436949293602493928401919081908401838280828437509497506105699650505050505050565b60408051600160a060020a039092168252519081900360200190f35b34801561029157600080fd5b506100f6600160a060020a03600435166105da565b3480156102b257600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526101db94369492936024939284019190819084018382808284375050604080516020601f89358b018035918201839004830284018301909452808352979a99988101979196509182019450925082915084018382808284375050604080516020601f89358b018035918201839004830284018301909452808352979a99988101979196509182019450925082915084018382808284375094975061064c9650505050505050565b34801561038757600080fd5b506040805160206004803580820135601f81018490048402850184019095528484526100f694369492936024939284019190819084018382808284375094975061080e9650505050505050565b3480156103e057600080fd5b506100f6600160a060020a0360043516610826565b6060600061040283610569565b905061040d81610826565b9392505050565b60005433600160a060020a039081169116141561043957600054600160a060020a0316ff5b565b60006001826040518082805190602001908083835b6020831061046f5780518252601f199092019160209182019101610450565b51815160209384036101000a600019018019909216911617905292019485525060405193849003019092205474010000000000000000000000000000000000000000900460ff16949350505050565b600160a060020a0381166000908152600260208181526040928390208201805484516001821615610100026000190190911693909304601f8101839004830284018301909452838352606093909183018282801561055d5780601f106105325761010080835404028352916020019161055d565b820191906000526020600020905b81548152906001019060200180831161054057829003601f168201915b50505050509050919050565b60006001826040518082805190602001908083835b6020831061059d5780518252601f19909201916020918201910161057e565b51815160209384036101000a6000190180199092169116179052920194855250604051938490030190922054600160a060020a0316949350505050565b600160a060020a038116600090815260026020818152604092839020805484516001821615610100026000190190911693909304601f8101839004830284018301909452838352606093909183018282801561055d5780601f106105325761010080835404028352916020019161055d565b604080516060810182528481526020808201859052818301849052600160a060020a0333166000908152600282529283208251805191926106929284929091019061089b565b5060208281015180516106ab926001850192019061089b565b50604082015180516106c791600284019160209091019061089b565b509050506106d48461043b565b15156107d257604080519081016040528033600160a060020a03168152602001600115158152506001856040518082805190602001908083835b6020831061072d5780518252601f19909201916020918201910161070e565b51815160209384036101000a600019018019909216911617905292019485525060405193849003810190932084518154959094015173ffffffffffffffffffffffffffffffffffffffff19909516600160a060020a039094169390931774ff00000000000000000000000000000000000000001916740100000000000000000000000000000000000000009415159490940293909317909155506001915061040d9050565b6040805160208181018084526000808452600160a060020a033316815260029092529290209051610803929061089b565b506000949350505050565b6060600061081b83610569565b905061040d816104be565b600160a060020a0381166000908152600260208181526040928390206001908101805485519281161561010002600019011693909304601f810183900483028201830190945283815260609390929183018282801561055d5780601f106105325761010080835404028352916020019161055d565b828054600181600116156101000203166002900490600052602060002090601f016020900481019282601f106108dc57805160ff1916838001178555610909565b82800160010185558215610909579182015b828111156109095782518255916020019190600101906108ee565b50610915929150610919565b5090565b61093391905b80821115610915576000815560010161091f565b905600a165627a7a7230582007b4a209af65a8219ef475c07ed4b1784292ea71ddd3164c4752439c600295770029";

    protected ReticulateUsers(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected ReticulateUsers(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public RemoteCall<byte[]> getPublicKeyForUserByName(String userName) {
        Function function = new Function("getPublicKeyForUserByName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
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

    public RemoteCall<Boolean> isUserNameTaken(String username) {
        Function function = new Function("isUserNameTaken", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(username)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}));
        return executeRemoteCallSingleValueReturn(function, Boolean.class);
    }

    public RemoteCall<String> getRootDirForUser(String addr) {
        Function function = new Function("getRootDirForUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getAddressForUserName(String userName) {
        Function function = new Function("getAddressForUserName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<String> getUserNameForAddress(String addr) {
        Function function = new Function("getUserNameForAddress", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<TransactionReceipt> registerUser(String username, byte[] pubKey, String rootDir) {
        Function function = new Function(
                "registerUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(username), 
                new org.web3j.abi.datatypes.DynamicBytes(pubKey), 
                new org.web3j.abi.datatypes.Utf8String(rootDir)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteCall<String> getRootDirForUserByName(String userName) {
        Function function = new Function("getRootDirForUserByName", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String(userName)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteCall<byte[]> getPublicKeyForUser(String addr) {
        Function function = new Function("getPublicKeyForUser", 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(addr)), 
                Arrays.<TypeReference<?>>asList(new TypeReference<DynamicBytes>() {}));
        return executeRemoteCallSingleValueReturn(function, byte[].class);
    }

    public static RemoteCall<ReticulateUsers> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateUsers.class, web3j, credentials, gasPrice, gasLimit, BINARY, "");
    }

    public static RemoteCall<ReticulateUsers> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return deployRemoteCall(ReticulateUsers.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, "");
    }

    public static ReticulateUsers load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateUsers(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    public static ReticulateUsers load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new ReticulateUsers(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }
}
